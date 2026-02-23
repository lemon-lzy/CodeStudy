package com.lzy.codestudybackend.model.dto.game;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @version 1.0
 * @Author lzy
 * @Date 2024/1/10 16:01
 */
@Data
public class GameDetail implements Serializable
{
    /**
     * 竞赛id
     */
    private Long gameId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 题目提交详情
     * key：题目id
     * val：最优答题情况
     */
    private Map<Long, GameDetailUnit> submitDetail;

    private static final long serialVersionUID = 1L;
}
