package com.lzy.codestudybackend.model.dto.game;

import com.lzy.codestudybackend.model.dto.questionSubmit.QuestionSubmitAddRequest;
import lombok.Data;

/**
 * @version 1.0
 * @Author lzy
 * @Date 2024/1/10 10:46
 */
@Data
public class GameQuestionSubmitRequest
{
    /**
     * 竞赛id
     */
    private Long gameId;

    /**
     * 题目提交信息
     */
    private QuestionSubmitAddRequest questionSubmitAddRequest;

    private static final long serialVersionUID = 1L;
}
