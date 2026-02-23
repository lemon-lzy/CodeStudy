package sspu.zzx.sspuoj.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author ZZX
 * @from SSPU
 */
@Data
public class UserRegisterRequest implements Serializable
{

    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userPassword;

    private String userName;

    private String userEmail;

    private String verifyCode;
}
