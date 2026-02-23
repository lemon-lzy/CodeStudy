package sspu.zzx.sspuoj.service;

import sspu.zzx.sspuoj.model.entity.ArticleLikes;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author ZZX
 * @description 针对表【article_likes】的数据库操作Service
 * @createDate 2023-12-12 15:14:24
 */
public interface ArticleLikesService extends IService<ArticleLikes>
{
    /**
     * 获得文章的点赞次数
     *
     * @param articleId
     * @return
     */
    Long getArticleLikesCount(Long articleId);

    /**
     * 用户点赞或取消点赞
     * 返回true：
     * 1. 用户点赞了文章
     * 返回false：
     * 2. 用户取消点赞了文章
     *
     * @param articleId
     * @param userId
     * @return
     */
    Boolean likeArticleOrNot(Long articleId, Long userId);

    /**
     * 判断该用户是否点赞了该文章
     *
     * @param articleId
     * @param userId
     * @return
     */
    Boolean ifLiked(Long articleId, Long userId);
}
