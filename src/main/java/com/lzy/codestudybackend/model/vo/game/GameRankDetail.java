package com.lzy.codestudybackend.model.vo.game;

import com.lzy.codestudybackend.model.dto.game.GameDetailUnit;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @version 1.0
 * @Author lzy
 * @Date 2024/1/10 16:06
 */
@Data
public class GameRankDetail implements Serializable
{
    /**
     * 名次
     */
    private Integer rankOrder;

    /**
     * 答题者id
     */
    private Long userId;

    /**
     * 答题者昵称
     */
    private String userName;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 总耗时
     */
    private Integer totalTime;

    /**
     * 总耗用内存
     */
    private Integer totalMemory;

    /**
     * 最优答题情况集合
     */
    private List<GameDetailUnit> questionDetails;

    private static final long serialVersionUID = 1L;
}
