package sspu.zzx.sspuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import sspu.zzx.sspuoj.model.dto.article.ArticleCommentQueryRequest;
import sspu.zzx.sspuoj.model.entity.ArticleComment;

/**
 * @author ZZX
 * @description 针对表【article_comment】的数据库操作Service
 * @createDate 2023-12-15 10:25:32
 */
public interface ArticleCommentService extends IService<ArticleComment>
{

    QueryWrapper<ArticleComment> getQueryWrapper(ArticleCommentQueryRequest articleCommentQueryRequest);

    Boolean reply(ArticleComment articleComment);

    Boolean commentModeration(String comment);
}
