package sspu.zzx.sspuoj.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import sspu.zzx.sspuoj.aop.annotation.OpLog;
import sspu.zzx.sspuoj.common.*;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.exception.ThrowUtils;
import sspu.zzx.sspuoj.model.dto.user.*;
import sspu.zzx.sspuoj.model.entity.OperationLog;
import sspu.zzx.sspuoj.model.entity.User;
import sspu.zzx.sspuoj.model.vo.user.LoginUserVO;
import sspu.zzx.sspuoj.model.vo.user.OnlineUserVO;
import sspu.zzx.sspuoj.model.vo.user.UserVO;
import sspu.zzx.sspuoj.service.OperationLogService;
import sspu.zzx.sspuoj.service.UserFollowService;
import sspu.zzx.sspuoj.service.UserService;
import sspu.zzx.sspuoj.utils.MD5Utils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static sspu.zzx.sspuoj.constant.UserConstant.ADMIN_ROLE;

/**
 * 用户接口
 *
 * @author ZZX
 * @from SSPU
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserMicroController
{

    @Resource
    private UserService userService;
    @Resource
    private VerifyMicroController verifyMicroController;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private UserFollowService userFollowService;

    /*用户：UserController*/

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @OpLog("用户注册:user")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest)
    {
        if (userRegisterRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String userName = userRegisterRequest.getUserName();
        String userEmail = userRegisterRequest.getUserEmail();
        String verifyCode = userRegisterRequest.getVerifyCode();

        if (StringUtils.isAnyBlank(userAccount, userPassword, userName, userEmail, verifyCode))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检验邮箱验证码是否过期或正确
        verifyMicroController.checkEmailCode(userEmail, verifyCode);
        long result = userService.userRegister(userAccount, userPassword, userName, userEmail, verifyCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @return
     */
    @OpLog("用户登录:user")
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest)
    {
        if (userLoginRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        String verifyCode = userLoginRequest.getVerifyCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, verifyCode))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检测验证码
        verifyMicroController.checkVerify(verifyCode);
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 找回密码
     *
     * @param userFindPwdRequest
     * @return
     */
    @OpLog("用户密码找回:user")
    @PostMapping("/findPwd")
    public BaseResponse<Boolean> userFindPwd(@RequestBody UserFindPwdRequest userFindPwdRequest)
    {
        if (userFindPwdRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userFindPwdRequest.getUserAccount();
        String newPassword = userFindPwdRequest.getNewPassword();
        String userEmail = userFindPwdRequest.getUserEmail();
        String verifyCode = userFindPwdRequest.getVerifyCode();
        if (StringUtils.isAnyBlank(userAccount, newPassword, userEmail, verifyCode))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检验邮箱验证码是否过期或正确
        verifyMicroController.checkEmailCode(userEmail, verifyCode);
        boolean result = userService.userFindPwd(userAccount, newPassword, userEmail);
        return ResultUtils.success(result);
    }

    /**
     * 用户注销
     *
     * @return
     */
    @SaCheckLogin
    @OpLog("用户注销:user")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout()
    {
        boolean result = userService.userLogout();
        return ResultUtils.success(result);
    }

    /**
     * 获得当前登录信息
     *
     * @return
     */
    @SaCheckLogin
    @GetMapping("/get/loginInfo")
    public BaseResponse<LoginUserVO> getLoginUserInfo()
    {
        LoginUserVO loginUserVo = userService.getLoginUserVoBySa();
        return ResultUtils.success(loginUserVo);
    }

    /**
     * 根据userId获得用户信息
     *
     * @return
     */
    @SaCheckLogin
    @GetMapping("/get/loginInfoByUserId")
    public BaseResponse<LoginUserVO> getLoginUserInfoByUserId(@RequestParam("userId") Long userId, @RequestParam("containFans") Boolean containFans)
    {
        LoginUserVO loginUserVo = userService.getLoginUserVoByUserId(userId, containFans);
        return ResultUtils.success(loginUserVo);
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request)
    {
        if (userAddRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    // @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request)
    {
        if (deleteRequest == null || deleteRequest.getId() <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @return
     */
    @SaCheckRole(ADMIN_ROLE)
    @OpLog("管理员更新用户信息:user")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest)
    {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
		if(StringUtils.isNotBlank(user.getUserAvatar()))
        {
            user.setUserAvatar(user.getUserAvatar().replace("0.0.0.0","127.0.0.1"));
        }
        // 特判pwd
        this.handleUserPwd(user, userUpdateRequest.getUserPassword(), true);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @SaCheckRole(ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request)
    {
        if (id <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request)
    {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @SaCheckRole(ADMIN_ROLE)
    @PostMapping("/list/page")
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request)
    {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryRequest));
        // 判断是否被封号
        userPage.setRecords(userPage.getRecords().stream().peek(record -> record.setBan(StpUtil.isDisable(record.getId()))).collect(Collectors.toList()));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request)
    {
        if (userQueryRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    // region 用户个体信息编辑

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @return
     */
    @SaCheckLogin
    @OpLog("用户更新个人信息:user")
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest)
    {
        if (userUpdateMyRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
		if(StringUtils.isNotBlank(user.getUserAvatar()))
        {
            user.setUserAvatar(user.getUserAvatar().replace("0.0.0.0","127.0.0.1"));
        }
        // 特判pwd
        this.handleUserPwd(user, userUpdateMyRequest.getNewPassword(), false);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    public void handleUserPwd(User user, String newPwd, boolean isAdmin)
    {
        String password = user.getUserPassword();
        if (StringUtils.isNotBlank(password))
        {
            if (password.length() < 6)
            {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短，长度至少为6！");
            } else
            {
                // 如果不是admin，判断密码是否正确再决定是否修改
                if (!isAdmin)
                {
                    User userDb = userService.getById(user.getId());
                    String encode = MD5Utils.encode(userDb.getUserSalt() + password);
                    if (!encode.equals(userDb.getUserPassword()))
                    {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码输入有误！");
                    }
                }
                String salt = MD5Utils.getSalt();
                String encryptPassword = MD5Utils.encode(salt + newPwd);
                user.setUserSalt(salt);
                user.setUserPassword(encryptPassword);
            }
        } else
        {
            user.setUserPassword(null);
        }
    }

    // endregion

    // region 在线用户管理

    /**
     * 获得当前在线用户
     *
     * @param pageRequest
     * @return
     */
    @SaCheckRole(ADMIN_ROLE)
    @PostMapping("/user/online")
    public BaseResponse<Map<Long, List<OnlineUserVO>>> getOnlineUserList(@RequestBody PageRequest pageRequest)
    {
        if (pageRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = pageRequest.getCurrent();
        long size = pageRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 获得当前登录的全部token对应的loginId
        /*
        List<Object> loginList = StpUtil.searchTokenValue("", 0, -1, true)
                .stream()
                .map(item ->
                {
                    String token = item != null ? item.toString().substring(item.toString().lastIndexOf(":") + 1) : null;
                    return StpUtil.getLoginIdByToken(token);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
         */
        List<String> tokenList = StpUtil.searchTokenValue("", 0, -1, true);
        List<String> tokenValueList = tokenList.stream().map(item -> item.toString().substring(item.toString().lastIndexOf(":") + 1)).collect(Collectors.toList());
        // 获得所有在线的用户
        // List<OperationLog> records = operationLogService.getOnlineUserList(loginList);
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("operationName", "用户登录");
        queryWrapper.eq("tName", "user");
        queryWrapper.in("operatorToken", tokenValueList);
        List<OperationLog> records = operationLogService.list(queryWrapper);
        List<OnlineUserVO> onlineUserVOList = records.stream().map(record ->
        {
            OnlineUserVO onlineUserVO = new OnlineUserVO();
            onlineUserVO.setId(record.getOperatorId());
            User byId = userService.getById(record.getOperatorId());
            onlineUserVO.setUserName(byId.getUserName());
            onlineUserVO.setUserType(byId.getUserType());
            onlineUserVO.setLoginIp(record.getOperatorIp());
            onlineUserVO.setOperatorBrowser(record.getOperatorBrowser());
            onlineUserVO.setOperatorClient(record.getOperatorClient());
            onlineUserVO.setOperatorOs(record.getOperatorOs());
            onlineUserVO.setOperatorToken(record.getOperatorToken());
            onlineUserVO.setLoginTime(record.getCreateTime());
            return onlineUserVO;
        }).collect(Collectors.toList());
        // 根据当前页面和页数裁剪list
        int startIndex = (int) ((current - 1) * size);
        int endIndex = Math.min((int) (startIndex + size), onlineUserVOList.size());
        Map<Long, List<OnlineUserVO>> map = new HashMap<>();
        map.put((long) records.size(), onlineUserVOList.subList(startIndex, endIndex));
        return ResultUtils.success(map);
    }

    /**
     * 强制踢人下线(（清楚token）
     *
     * @param loginToken
     * @return
     */
    @OpLog("管理员踢人下线:user")
    @SaCheckRole(ADMIN_ROLE)
    @PostMapping("/user/kick")
    public BaseResponse<Boolean> kickOnlineUserByLoginToken(@RequestParam String loginToken)
    {
        if (StringUtils.isBlank(loginToken))
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "loginToken参数错误");
        }
        StpUtil.logoutByTokenValue(loginToken);
        return ResultUtils.success(true);
    }


    /**
     * 封号
     *
     * @param userId
     * @return
     */
    @OpLog("管理员封号:user")
    @SaCheckRole(ADMIN_ROLE)
    @PostMapping("/user/ban")
    public BaseResponse<Boolean> banUserByUserId(@RequestParam Long userId)
    {
        // 先踢下线
        StpUtil.kickout(userId);
        // 永久封禁指定账号
        StpUtil.disable(userId, -1);
        return ResultUtils.success(true);
    }

    /**
     * 解封
     *
     * @param userId
     * @return
     */
    @OpLog("管理员解封:user")
    @SaCheckRole(ADMIN_ROLE)
    @PostMapping("/user/free")
    public BaseResponse<Boolean> freeUserById(@RequestParam Long userId)
    {
        // 解除封禁
        StpUtil.untieDisable(userId);
        return ResultUtils.success(true);
    }


    // endregion

    /*用户关注：UserFollowController*/

    /**
     * 关注 todo new
     *
     * @param followerId
     * @param followeeId
     * @return
     */
    @OpLog("用户关注:user_follow")
    @PostMapping("/following/follow")
    public BaseResponse<Boolean> follow(@RequestParam("followerId") Long followerId, @RequestParam("followeeId") Long followeeId)
    {
        if (ObjectUtils.isEmpty(followeeId) || ObjectUtils.isEmpty(followerId))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userFollowService.follow(followerId, followeeId));
    }

    /**
     * 取关 todo new
     *
     * @param followerId
     * @param followeeId
     * @return
     */
    @OpLog("用户取关:user_follow")
    @PostMapping("/following/unfollow")
    public BaseResponse<Boolean> unfollow(@RequestParam("followerId") Long followerId, @RequestParam("followeeId") Long followeeId)
    {
        if (ObjectUtils.isEmpty(followeeId) || ObjectUtils.isEmpty(followerId))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userFollowService.unfollow(followerId, followeeId));
    }

}
