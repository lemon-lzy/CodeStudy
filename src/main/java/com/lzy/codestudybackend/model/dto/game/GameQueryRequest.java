package com.lzy.codestudybackend.model.dto.game;

import com.lzy.codestudybackend.common.PageRequest;
import lombok.Data;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2024/1/10 10:46
 */
@Data
public class GameQueryRequest extends PageRequest
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
     * 时间范围开始
     */
    private String startTime;

    /**
     * 时间范围结束
     */
    private String endTime;

    private static final long serialVersionUID = 1L;
}
