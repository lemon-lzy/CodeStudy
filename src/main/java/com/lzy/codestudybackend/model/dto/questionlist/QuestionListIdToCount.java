package com.lzy.codestudybackend.model.dto.questionlist;

import lombok.Data;

/**
 * @version 1.0
 * @Author lzy
 * @Date 2023/12/25 21:36
 */
@Data
public class QuestionListIdToCount
{
    /**
     * 题单id
     */
    private Long listId;

    /**
     * 题目数量
     */
    private Long questionCount;
}