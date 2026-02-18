package com.lzy.codestudybackend.model.dto.questionlist;

import com.lzy.codestudybackend.common.PageRequest;
import lombok.Data;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/12/25 20:19
 */
@Data
public class QuestionListQueryRequest extends PageRequest
{
    /**
     * 题单id
     */
    private Long id;

    /**
     * 题单名称
     */
    private String listName;

    /**
     * 题单作者id
     */
    private Long creatorId;

    /**
     * 创作者姓名
     */
    private String creatorName;

    /**
     * 可见范围
     * 1. all:所有人可见
     * 2. private：仅自己可见
     * 3. 。。。
     */
    private String publicZone = "all";

    private static final long serialVersionUID = 1L;
}