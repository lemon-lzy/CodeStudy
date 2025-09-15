package com.lzy.codestudybackend.model.dto.questionSubmit;

import lombok.Data;

@Data
public class QuestionSubmitMqAddRequest {

    /**
     * 题目 id
     */
    private Long questionId;
    /**
     * 提交题目Id
     */
    private Long questionSubmitId;

    /**
     * 编程语言
     */
    private String submitLanguage;

    /**
     * 用户提交代码
     */
    private String submitCode;

    /**
     * 输入数据
     */
    private String inputList;

    private static final long serialVersionUID = 1L;
}
