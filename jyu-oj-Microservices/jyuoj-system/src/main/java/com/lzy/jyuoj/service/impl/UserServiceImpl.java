package sspu.zzx.sspuoj.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.constant.CommonConstant;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.mapper.UserMapper;
import sspu.zzx.sspuoj.model.dto.user.UserQueryRequest;
import sspu.zzx.sspuoj.model.entity.User;
import sspu.zzx.sspuoj.model.entity.UserOrgan;
import sspu.zzx.sspuoj.model.enums.user.UserInfoEnum;
import sspu.zzx.sspuoj.model.enums.user.UserRoleEnum;
import sspu.zzx.sspuoj.model.vo.user.LoginUserVO;
import sspu.zzx.sspuoj.model.vo.user.UserVO;
import sspu.zzx.sspuoj.service.AvatarService;
import sspu.zzx.sspuoj.service.UserFollowService;
import sspu.zzx.sspuoj.service.UserOrganService;
import sspu.zzx.sspuoj.service.UserService;
import sspu.zzx.sspuoj.utils.MD5Utils;
import sspu.zzx.sspuoj.utils.NetUtils;
import sspu.zzx.sspuoj.utils.SqlUtils;
import sspu.zzx.sspuoj.utils.TokenUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static sspu.zzx.sspuoj.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 *
 * @author ZZX
 * @from SSPU
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService
{

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "zzx";

    @Autowired
    private TokenUtils tokenUtils;
    @Autowired
    private AvatarService avatarService;
    @Autowired
    private UserOrganService userOrganService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserFollowService userFollowService;

    @Override
    public long userRegister(String userAccount, String userPassword, String userName, String userEmail, String verifyCode)
    {
        // 1. 校验
        if (userName.length() > 6)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名过短，长度多为6！");
        }
        if (userAccount.length() < 4)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短，长度至少为4！");
        }
        if (userPassword.length() < 6)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短，长度至少为6！");
        }
        synchronized (userAccount.intern())
        {
            // 账户和邮箱不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount).or().eq("userEmail", userEmail);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0)
            {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或邮箱不能重复！");
            }
            // 2. 加密
            String salt = MD5Utils.getSalt();
            String encryptPassword = MD5Utils.encode(salt + userPassword);
            // 3. 插入数据
            User user = new User();
            user.setUserName(userName);
            user.setUserAccount(userAccount);
            user.setUserEmail(userEmail);
            user.setUserAvatar(UserInfoEnum.USER_DEFAULT_AVATAR.getValue());
            user.setUserProfile(UserInfoEnum.USER_DEFAULT_PROFILE.getValue());
            user.setUserType(UserInfoEnum.USER_DEFAULT_TYPE.getValue());
            user.setUserSalt(salt);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult)
            {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误!");
            }
            // 及时移除邮箱验证码
            tokenUtils.removeToken(userEmail);
            // 异步更新头像
            Thread thread = new Thread(() ->
            {
                try
                {
                    String avatarByFullName = avatarService.getDefaultAvatarByFullName(user.getUserName());
                    user.setUserAvatar(avatarByFullName);
                    this.updateById(user);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            });
            thread.start();
            return user.getId();
        }
    }

    @Override
    public boolean userFindPwd(String userAccount, String newPassword, String userEmail)
    {
        // 1. 校验
        if (userAccount.length() < 4)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短，长度至少为4！");
        }
        if (newPassword.length() < 6)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短，长度至少为6！");
        }
        // 2. 检查数据
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userEmail", userEmail);
        User user = this.getOne(queryWrapper);
        if (user == null)
        {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "账号或邮箱不存在！");
        }
        // 3. 更新数据
        String salt = MD5Utils.getSalt();
        String encryptPassword = MD5Utils.encode(salt + newPassword);
        user.setUserSalt(salt);
        user.setUserPassword(encryptPassword);
        // 及时移除邮箱验证码
        tokenUtils.removeToken(userEmail);
        return this.updateById(user);
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword)
    {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空！");
        }
        // 2. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null)
        {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在！");
        }
        // 3. 加密，进一步检验
        // 判断密码是否正确（加盐后）
        String loginSalt = user.getUserSalt();
        String loginPwd = MD5Utils.encode(loginSalt + userPassword);
        if (!loginPwd.equals(user.getUserPassword()))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误，请重试！");
        }
        log.info("SSPU-OJ:用户【" + user.getUserName() + "】于" + new Date() + "登录成功！");
        Map<String, String> userAgentInfo = NetUtils.getUserAgentInfo(true);
        // 设置登录方式
        // 调用sa-token的登录方法
        // 会话登录：参数填写要登录的账号id，建议的数据类型：long | int | String， 不可以传入复杂类型，如：User、Admin 等等
        // 检测是否被封禁
        // 校验指定账号是否已被封禁，如果被封禁则抛出异常 `DisableServiceException`
        StpUtil.checkDisable(user.getId());
        StpUtil.login(user.getId(), userAgentInfo.get("operatorClient"));
        // 设置token
        user.setSaToken(StpUtil.getTokenValue());
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO getLoginUserVoBySa()
    {
        // 获取当前会话账号id, 并转化为`long`类型
        String userId = StpUtil.getLoginIdAsString();
        User user = this.getById(userId);
        user.setSaToken(StpUtil.getTokenValue());
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO getLoginUserVoByUserId(Long userId, Boolean containFans)
    {
        User user = this.getById(userId);
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        if (containFans)
        {
            return getLoginUserVOContainFans(loginUserVO);
        }
        return loginUserVO;
    }


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request)
    {
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        User currentUser = this.getById(StpUtil.getLoginIdAsLong());
        if (currentUser == null)
        {
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
    public User getLoginUserPermitNull(HttpServletRequest request)
    {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null)
        {
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
    public boolean isAdmin(HttpServletRequest request)
    {
        // 仅管理员可查询
        return isAdmin(getLoginUser(null));
    }

    @Override
    public boolean isAdmin(User user)
    {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserType());
    }

    /**
     * 用户注销
     */
    @Override
    public boolean userLogout()
    {
        Object loginIdDefaultNull = StpUtil.getLoginIdDefaultNull();
        StpUtil.logout();
        log.info("SSPU-OJ：用户于" + new Date() + "退出登录！" + loginIdDefaultNull);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user)
    {
        if (user == null)
        {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        // 获取当前用户所在组织id和组织名称
        QueryWrapper<UserOrgan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", user.getId());
        queryWrapper.eq("status", "在职");
        UserOrgan one = userOrganService.getOne(queryWrapper);
        if (one != null)
        {
            loginUserVO.setOrganId(one.getOrganId());
        }
        return loginUserVO;
    }

    public LoginUserVO getLoginUserVOContainFans(LoginUserVO loginUserVO)
    {
        if (loginUserVO == null)
        {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // 获得当前userId
        Long userVOId = loginUserVO.getId();
        // 作为followeeId，进而去找到粉丝列表
        List<UserVO> followerList = userFollowService.getFollowerList(userVOId);
        loginUserVO.setFollowerList(followerList);
        // 作为followerId，进而去找到关注列表
        List<UserVO> followeeList = userFollowService.getFolloweeList(userVOId);
        loginUserVO.setFolloweeList(followeeList);

        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user)
    {
        if (user == null)
        {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList)
    {
        if (CollectionUtils.isEmpty(userList))
        {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
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

    @Override
    public Boolean ifAdminOrSelf(Long userId)
    {
        if (isAdmin((HttpServletRequest) null))
        {
            return true;
        }
        User loginUser = getLoginUser(null);
        return loginUser.getId().equals(userId);
    }
}
