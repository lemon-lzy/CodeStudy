package sspu.zzx.sspuoj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName organization
 */
@TableName(value = "organization")
@Data
public class Organization implements Serializable
{
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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
     * 组织当前人数
     */
    private Integer organCurrentNum;

    /**
     * 组织最大人数
     */
    private Integer organTotalNum;

    /**
     * 组织创始人Id
     */
    private Long organizerId;

    /**
     * 组织创始人名称
     */
    private String organizerName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}