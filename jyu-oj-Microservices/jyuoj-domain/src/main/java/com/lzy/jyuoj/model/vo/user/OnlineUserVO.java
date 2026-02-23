package sspu.zzx.sspuoj.model.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 在线用户视图（脱敏）
 *
 * @author ZZX
 * @from SSPU
 */
@Data
public class OnlineUserVO implements Serializable
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
     * 用户类型:admin/user/ban
     */
    private String userType;

    /**
     * 操作者浏览器
     */
    private String operatorBrowser;

    /**
     * 操作者客户端
     */
    private String operatorClient;

    /**
     * 操作者系统
     */
    private String operatorOs;

    /**
     * 操作者token
     */
    private String operatorToken;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 登录时间
     */
    private Date loginTime;


    private static final long serialVersionUID = 1L;
}