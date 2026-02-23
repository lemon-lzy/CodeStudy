package sspu.zzx.sspuoj.model.dto.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sspu.zzx.sspuoj.common.PageRequest;

import java.io.Serializable;

/**
 * 用户查询请求
 *
 * @author ZZX
 * @from SSPU
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     *  用户邮箱
     */
    private String userEmail;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户类型：user/admin
     */
    private String userType;

    private static final long serialVersionUID = 1L;
}