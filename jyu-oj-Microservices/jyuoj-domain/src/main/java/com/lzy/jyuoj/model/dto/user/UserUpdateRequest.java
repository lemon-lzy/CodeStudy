package sspu.zzx.sspuoj.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 *
 * @author ZZX
 * @from SSPU
 */
@Data
public class UserUpdateRequest implements Serializable
{
    /**
     * id
     */
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
     * 用户邮箱（todo 加一个核验邮箱是否可用功能）
     */
    private String userEmail;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 用户类型：user/admin
     */
    private String userType;

    /**
     * 创作权集合
     */
    private String createRights;

    private static final long serialVersionUID = 1L;
}