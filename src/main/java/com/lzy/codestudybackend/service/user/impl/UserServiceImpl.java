package com.lzy.codestudybackend.service.user.impl;

import static com.lzy.codestudybackend.constant.UserConstant.USER_LOGIN_STATE;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.codestudybackend.common.ErrorCode;
import com.lzy.codestudybackend.constant.CommonConstant;
import com.lzy.codestudybackend.constant.RedisConstant;
import com.lzy.codestudybackend.exception.BusinessException;
import com.lzy.codestudybackend.mapper.user.UserMapper;
import com.lzy.codestudybackend.model.dto.user.UserQueryRequest;
import com.lzy.codestudybackend.model.entity.user.User;
import com.lzy.codestudybackend.model.enums.UserRoleEnum;
import com.lzy.codestudybackend.model.vo.LoginUserVO;
import com.lzy.codestudybackend.model.vo.UserVO;
import com.lzy.codestudybackend.service.user.UserService;
import com.lzy.codestudybackend.utils.SqlUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现
 *
 * @author lzy
 * 
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "lzy";

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        // 单机锁
        synchronized (unionId.intern()) {
            // 查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("unionId", unionId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setUnionId(unionId);
                user.setMpOpenId(mpOpenId);
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            // 记录用户的登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            return getLoginUserVO(user);
        }
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }


    /**
     * 添加用户签到记录
     * @param userId 用户 id
     * @return 当前用户是否已签到成功
     */
    @Override
    public boolean addUserSignIn(long userId) {
        LocalDate date = LocalDate.now();
        String key = RedisConstant.getUserSignInRedisKey(date.getYear(), userId);
        // 获取 Redis 的 BitMap
        RBitSet signInBitSet = redissonClient.getBitSet(key);
        // 获取当前日期是一年中的第几天，作为偏移量（从 1 开始计数）
        int offset = date.getDayOfYear();
        // 查询当天有没有签到
        if (!signInBitSet.get(offset)) {
            // 如果当前未签到，则设置
            signInBitSet.set(offset, true);
        }
        // 更新排行榜数据
        updateSignInRank(userId, date);
        // 当天已签到
        return true;
    }

    /**
     * 获取用户某个年份的签到记录
     *
     * @param userId 用户 id
     * @param year   年份（为空表示当前年份）
     * @return 签到记录映射
     */
    @Override
    public List<Integer> getUserSignInRecord(long userId, Integer year) {
        if (year == null) {
            LocalDate date = LocalDate.now();
            year = date.getYear();
        }
        String key = RedisConstant.getUserSignInRedisKey(year, userId);
        // 获取 Redis 的 BitMap
        RBitSet signInBitSet = redissonClient.getBitSet(key);
        // 加载 BitSet 到内存中，避免后续读取时发送多次请求
        BitSet bitSet = signInBitSet.asBitSet();
        // 统计签到的日期
        List<Integer> dayList = new ArrayList<>();
        // 从索引 0 开始查找下一个被设置为 1 的位
        int index = bitSet.nextSetBit(0);
        while (index >= 0) {
            dayList.add(index);
            // 继续查找下一个被设置为 1 的位
            index = bitSet.nextSetBit(index + 1);
        }
        return dayList;
    }

    /**
     * 更新签到排行榜 - 新增方法
     * @param userId 用户ID
     * @param date 签到日期
     */
    private void updateSignInRank(long userId, LocalDate date) {
        // 1. 更新总排行榜
        updateTotalRank(userId);

        // 2. 更新年排行榜
        updateYearRank(userId, date.getYear());

        // 3. 更新月排行榜
        updateMonthRank(userId, date.getYear(), date.getMonthValue());
    }

    /**
     * 更新总排行榜
     * @param userId 用户ID
     */
    private void updateTotalRank(long userId) {
        String totalRankKey = RedisConstant.getUserSignInTotalRankKey();

        // 检查用户是否已在排行榜中
        Double currentScore = redisTemplate.opsForZSet().score(totalRankKey, userId);

        if (currentScore == null) {
            // 用户不在排行榜中，第一次签到，设置分数为1
            redisTemplate.opsForZSet().add(totalRankKey, userId, 1);
            log.info("Initialized total rank for user {}, score: 1", userId);
        } else {
            // 用户已在排行榜中，分数+1
            int newScore = currentScore.intValue() + 1;
            redisTemplate.opsForZSet().add(totalRankKey, userId, newScore);
            log.info("Updated total rank for user {}, new score: {}", userId, newScore);
        }
    }

    /**
     * 更新年排行榜
     * @param userId 用户ID
     * @param year 年份
     */
    private void updateYearRank(long userId, int year) {
        String yearRankKey = RedisConstant.getUserSignInRankKey(year);

        // 检查用户是否已在年排行榜中
        Double currentScore = redisTemplate.opsForZSet().score(yearRankKey, userId);

        if (currentScore == null) {
            // 用户不在年排行榜中，今年第一次签到，设置分数为1
            redisTemplate.opsForZSet().add(yearRankKey, userId, 1);
            log.info("Initialized year rank for user {} in year {}, score: 1", userId, year);
        } else {
            // 用户已在年排行榜中，分数+1
            int newScore = currentScore.intValue() + 1;
            redisTemplate.opsForZSet().add(yearRankKey, userId, newScore);
            log.info("Updated year rank for user {} in year {}, new score: {}", userId, year, newScore);
        }
    }

    /**
     * 更新月排行榜
     * @param userId 用户ID
     * @param year 年份
     * @param month 月份
     */
    private void updateMonthRank(long userId, int year, int month) {
        String monthRankKey = RedisConstant.getUserSignInMonthlyRankKey(year, month);

        // 检查用户是否已在月排行榜中
        Double currentScore = redisTemplate.opsForZSet().score(monthRankKey, userId);

        if (currentScore == null) {
            // 用户不在月排行榜中，本月第一次签到，设置分数为1
            redisTemplate.opsForZSet().add(monthRankKey, userId, 1);
            log.info("Initialized month rank for user {} in {}-{}, score: 1", userId, year, month);
        } else {
            // 用户已在月排行榜中，分数+1
            int newScore = currentScore.intValue() + 1;
            redisTemplate.opsForZSet().add(monthRankKey, userId, newScore);
            log.info("Updated month rank for user {} in {}-{}, new score: {}", userId, year, month, newScore);
        }
    }


    /**
     * 获取用户签到排行榜
     * @param limit 数量
     * @param year 年份
     * @param month 月份
     * @return 用户信息列表
     */
    @Override
    public List<UserVO> getUserSignInRank(Integer limit, Integer year,Integer month)
    {
        if (limit == null) {
            limit = 10;
        }

        String rankKey;
        if (year == null) {
            // 总排行榜
            rankKey = RedisConstant.getUserSignInTotalRankKey();
        } else if (month == null) {
            // 年度排行榜
            rankKey = RedisConstant.getUserSignInRankKey(year);
        } else {
            // 月度排行榜
            rankKey = RedisConstant.getUserSignInMonthlyRankKey(year, month);
        }

        // 从Redis获取排行榜
        Set<Object> rankSet = redisTemplate.opsForZSet().reverseRange(rankKey, 0, limit - 1);

        if (rankSet == null || rankSet.isEmpty()) {
            // Redis中没有数据，返回空列表
            // 注意：在实际应用中，可能需要考虑初始化排行榜的逻辑
            log.warn("No rank data found in Redis for key: {}", rankKey);
            return new ArrayList<>();
        }

        // 获取所有用户ID列表
        List<Long> userIds = rankSet.stream()
                .map(id -> Long.parseLong(id.toString()))
                .collect(Collectors.toList());

        // 直接从数据库批量查询用户信息
        List<User> userList = this.listByIds(userIds);

        // 构建用户ID到UserVO的映射
        Map<Long, UserVO> userMap = userList.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        this::getUserVO,
                        (existing, replacement) -> existing
                ));

        // 构建排行榜结果
        return rankSet.stream()
                .map(id -> {
                    Long userId = Long.parseLong(id.toString());
                    UserVO userVO = userMap.get(userId);

                    if (userVO != null) {
                        // 设置签到数
                        Double score = redisTemplate.opsForZSet().score(rankKey, id);
                        userVO.setSignInCount(score != null ? score.intValue() : 0);
                    }

                    return userVO;
                })
                .filter(Objects::nonNull) // 过滤掉null值
                .collect(Collectors.toList());
    }


    @Override
    public List<UserVO> getUserVOByUserIdList(List<Long> userIdList)
    {
        if (CollectionUtils.isEmpty(userIdList))
        {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, userIdList);
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest)
    {
        if (userQueryRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userEmail = userQueryRequest.getUserEmail();
        String userType = userQueryRequest.getUserType();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StringUtils.isNotBlank(userType), "userType", userType);
        queryWrapper.like(StringUtils.isNotBlank(userEmail), "userEmail", userEmail);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }
}
