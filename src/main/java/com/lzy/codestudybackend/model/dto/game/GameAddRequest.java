package com.lzy.codestudybackend.model.dto.game;

import lombok.Data;

import java.util.List;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2024/1/10 10:46
 */
@Data
public class GameAddRequest
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
     * 竞赛最大人数
     */
    private Integer gameTotalNum;

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
    private String startTime;

    /**
     * 竞赛截止时间
     */
    private String endTime;

    /**
     * 题目包含的题目id集合
     */
    private List<Long> questionIdList;

    /**
     * 题目的满分集合
     */
    private List<Integer> questionFullScoreList;

    private static final long serialVersionUID = 1L;
}
