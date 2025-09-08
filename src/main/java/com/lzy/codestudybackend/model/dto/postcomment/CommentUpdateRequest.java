package com.lzy.codestudybackend.model.dto.postcomment;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新评论请求
 */
@Data
public class CommentUpdateRequest implements Serializable {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
}