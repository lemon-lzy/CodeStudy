package sspu.zzx.sspuoj.model.dto.article;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sspu.zzx.sspuoj.common.PageRequest;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询请求
 *
 * @author ZZX
 * @from SSPU
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ArticleCommentQueryRequest extends PageRequest implements Serializable
{

    /**
     * id
     */
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否整合
     */
    private Boolean ifMerge = false;


    private static final long serialVersionUID = 1L;
}