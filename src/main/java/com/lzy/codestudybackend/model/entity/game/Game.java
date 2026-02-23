package com.lzy.codestudybackend.model.entity.game;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lzy
 * @TableName game
 */
@TableName(value = "game")
@Data
public class Game implements Serializable
{
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 竞赛名称
     */
    private String gameName;

    /**
     * 竞赛头像
     */
    private String gameAvatar;

    /**
     * 竞赛简介
     */
    private String gameProfile;

    /**
     * 竞赛当前人数
     */
    private Integer gameCurrentNum;

    /**
     * 竞赛最大人数
     */
    private Integer gameTotalNum;

    /**
     * 竞赛创始人id
     */
    private Long gameFounderId;

    /**
     * 竞赛创始人名称
     */
    private String gameFounderName;

    /**
     * 竞赛公开程度
     * 1. 公开：all
     * 2. 私有：private，仅自己和组织内可见
     * 3. ...
     */
    private String publicZone;

    /**
     * 竞赛类型
     * 1. 基础型
     * 2. 提高型
     * 3. 综合型
     * 4. ...
     */
    private String gameType;

    /**
     * 竞赛开始时间
     */
    private Date startTime;

    /**
     * 竞赛截止时间
     */
    private Date endTime;

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