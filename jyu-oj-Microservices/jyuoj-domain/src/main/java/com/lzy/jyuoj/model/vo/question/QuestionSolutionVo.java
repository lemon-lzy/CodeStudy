package sspu.zzx.sspuoj.model.vo.question;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题解列表封装类
 */
@Data
public class QuestionSolutionVo implements Serializable
{
    /**
     * id
     */
    private Long id;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 作者id
     */
    private Long userId;

    /**
     * 作者名称
     */
    private String authorName;

    /**
     * 作者头像链接
     */
    private String authorAvatar;

    /**
     * 题解标题
     */
    private String title;

    /**
     * 题解简介
     */
    private String introduction;


    /**
     * 题解点赞数
     */
    private Long solutionLikes;

    /**
     * 题解浏览数
     */
    private Long solutionViews;

    /**
     * 题解评论数
     */
    private Long solutionComments;

    /**
     * 题解类型
     */
    private String type;

    /**
     * 发布时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}