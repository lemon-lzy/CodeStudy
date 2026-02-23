package sspu.zzx.sspuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import sspu.zzx.sspuoj.common.BaseResponse;
import sspu.zzx.sspuoj.model.dto.user.UserQueryRequest;
import sspu.zzx.sspuoj.model.entity.User;
import sspu.zzx.sspuoj.model.vo.user.LoginUserVO;
import sspu.zzx.sspuoj.model.vo.user.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 * @author ZZX
 * @from SSPU
 */
public interface UserService extends IService<User>
{

    /**
     * 用户注册
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param userName     用户名称
     * @param userEmail    用户邮箱
     * @param verifyCode   邮箱验证码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String userName, String userEmail, String verifyCode);

    /**
     * 用户密码找回
     *
     * @param userAccount 用户账户
     * @param newPassword 用户密码
     * @param userEmail   用户邮箱
     * @return 新用户 id
     */
    boolean userFindPwd(String userAccount, String newPassword, String userEmail);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword);

    /**
     * 获取当前登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVoBySa();

    /**
     * 根据userId获取当前登录用户信息
     * 如果包含粉丝，则包含粉丝信息和关注者列表信息
     *
     * @return
     */
    LoginUserVO getLoginUserVoByUserId(Long userId, Boolean containFans);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @return
     */
    boolean userLogout();

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 根据userId列表获取脱敏的用户信息
     *
     * @param userIdList
     * @return
     */
    List<UserVO> getUserVOByUserIdList(List<Long> userIdList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 判断当前用户是否是管理员或某条记录的本人
     *
     * @param userId
     * @return
     */
    Boolean ifAdminOrSelf(Long userId);

}
