package sspu.zzx.sspuoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import lombok.Data;

/**
 * 
 * @TableName article_likes
 */
@TableName(value ="article_likes")
@Data
public class ArticleLikes implements Serializable {
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
     * 点赞人id
     */
    private Long userId;

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
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleLikes that = (ArticleLikes) o;
        return Objects.equals(id, that.id) && Objects.equals(articleId, that.articleId) && Objects.equals(userId, that.userId) && Objects.equals(createTime, that.createTime) && Objects.equals(updateTime, that.updateTime) && Objects.equals(isDelete, that.isDelete);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, articleId, userId, createTime, updateTime, isDelete);
    }
}