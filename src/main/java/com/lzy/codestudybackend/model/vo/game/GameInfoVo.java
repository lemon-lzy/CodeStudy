package com.lzy.codestudybackend.model.vo.game;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @version 1.0
 * @Author lzy
 * @Date 2024/1/10 11:25
 */
@Data
public class GameInfoVo implements Serializable
{
    /**
     * id
     */
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
     */
    private String publicZone;

    /**
     * 竞赛类型
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
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 题目数量（动态生成）
     */
    private Integer questionNum;


    private static final long serialVersionUID = 1L;
}
