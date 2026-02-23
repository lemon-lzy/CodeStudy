package sspu.zzx.sspuoj.model.vo.user;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 已登录用户视图（脱敏）
 *
 * @author ZZX
 * @from SSPU
 **/
@Data
public class LoginUserVO implements Serializable
{

    /**
     * 用户 id
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
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userType;

    /**
     * 组织编号
     */
    @TableField(exist = false)
    private Long organId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * Sa-token
     */
    private String saToken;

    /**
     * 粉丝信息列表
     */
    private List<UserVO> followerList;

    /**
     * 关注者信息列表（与粉丝相对）
     */
    private List<UserVO> followeeList;

    private static final long serialVersionUID = 1L;
}