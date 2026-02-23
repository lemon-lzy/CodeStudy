package sspu.zzx.sspuoj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName question_solution
 */
@TableName(value = "question_solution")
@Data
public class QuestionSolution implements Serializable
{
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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
     * 题解内容
     */
    private String solution;

    /**
     * 类型：题目、公告、日报等等
     */
    private String type;

    /**
     * 分享链接
     */
    private String sharingLink;

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
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}