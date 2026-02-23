package sspu.zzx.sspuoj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName question_list
 */
@TableName(value = "question_list")
@Data
public class QuestionList implements Serializable
{
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题单名称
     */
    private String listName;

    /**
     * 题单简介
     */
    private String listProfile;

    /**
     * 题单头像（图像链接）
     */
    private String listAvatar;

    /**
     * 题单可见区域
     * 1. all:所有人可见
     * 2. private：仅自己可见
     * 3. 。。。
     */
    private String publicZone;

    /**
     * 创建者id
     */
    private Long creatorId;

    /**
     * 创作者姓名
     */
    private String creatorName;

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