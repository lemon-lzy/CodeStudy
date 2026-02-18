package com.lzy.codestudybackend.model.dto.questionlist;

import lombok.Data;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/12/25 20:09
 */
@Data
public class QuestionListProfile
{
    /**
     * 文字简介
     */
    private String listDescription;

    /**
     * 插图简介链接
     */
    private String listImgUrl;

    /**
     * 视频简介链接（来自b站）
     */
    private String listVideoUrl;

    private static final long serialVersionUID = 1L;
}