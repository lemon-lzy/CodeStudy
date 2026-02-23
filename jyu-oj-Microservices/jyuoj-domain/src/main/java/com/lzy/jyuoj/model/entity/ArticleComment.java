package sspu.zzx.sspuoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * @TableName article_comment
 */
@TableName(value = "article_comment")
@Data
public class ArticleComment implements Serializable
{
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 发信者id
     */
    private Long senderId;

    /**
     * 收信者id
     */
    private Long receiverId;

    /**
     * 评论内容
     */
    private String comment;

    /**
     * 发信者名称
     */
    private String senderName;

    /**
     * 发信者头像链接
     */
    private String senderAvatar;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private List<ArticleComment> children;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}