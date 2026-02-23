package sspu.zzx.sspuoj.model.dto.organization;

import lombok.Data;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/12/28 13:45
 */
@Data
public class OrganizationSetupRequest
{
    /**
     * 组织Id
     */
    private Long id;

    /**
     * 组织名称
     */
    private String organName;

    /**
     * 组织头像（图片链接）
     */
    private String organAvatar;

    /**
     * 组织简介
     */
    private String organProfile;

    /**
     * 组织公告
     */
    private String organNotice;

    /**
     * 组织最大人数
     */
    private Integer organTotalNum;


    private static final long serialVersionUID = 1L;
}
