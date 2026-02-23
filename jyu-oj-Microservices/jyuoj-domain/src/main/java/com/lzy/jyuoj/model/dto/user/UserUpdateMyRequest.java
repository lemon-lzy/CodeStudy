package sspu.zzx.sspuoj.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新个人信息请求
 *
 * @author ZZX
 * @from SSPU
 */
@Data
public class UserUpdateMyRequest implements Serializable
{
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String confirmPassword;

    private static final long serialVersionUID = 1L;
}