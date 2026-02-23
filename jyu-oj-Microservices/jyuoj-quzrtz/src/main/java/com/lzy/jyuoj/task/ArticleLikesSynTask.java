package sspu.zzx.sspuoj.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sspu.zzx.sspuoj.model.entity.ArticleLikes;
import sspu.zzx.sspuoj.model.entity.QuestionSolution;
import sspu.zzx.sspuoj.service.QuestionSolutionService;
import sspu.zzx.sspuoj.service.impl.ArticleLikesServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/12/12 16:54
 */
@Component
@Slf4j
public class ArticleLikesSynTask
{
    @Autowired
    private QuestionSolutionService questionSolutionService;
    @Autowired
    private ArticleLikesServiceImpl articleLikesService;

    /**
     * 定时同步文章点赞信息
     */
    @Scheduled(cron = "0 0 12 */1 * *") // 每1天
//    @Scheduled(cron = "0 */1 * * * *") // 每一分钟执行一次
    public void synArticleLikes()
    {
        log.info("定时同步文章点赞信息 - " + new Date());
        // 获取所有title不是【外部图文】的文章
        QueryWrapper<QuestionSolution> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("title", "外部图文");
        List<QuestionSolution> articles = questionSolutionService.list(queryWrapper);
        // 获取所有文章点赞集合
        List<ArticleLikes> articleLikes = articleLikesService.list();
        // 按文章id分组，Map的值为List<ArticleLikes>
        Map<Long, List<ArticleLikes>> idToArticleLikesMap = articleLikes.stream().collect(Collectors.groupingBy(ArticleLikes::getArticleId));
        // 定义要更新的question_solution
        List<QuestionSolution> toUpdateSolution = new ArrayList<>();
        // 定义最终要删除和添加的点赞记录
        List<ArticleLikes> toDeleteArticleLikes = new ArrayList<>();
        List<ArticleLikes> toAddArticleLikes = new ArrayList<>();
        for (QuestionSolution article : articles)
        {
            // 从redis中文章id对应的点赞数
            Long articleLikesFromRedis = articleLikesService.getArticleLikes(article.getId());
            // 从redis中文章id对应的具体点赞用户集合
            List<Long> articleLikedUserIds = articleLikesService.getArticleLikedUsers(article.getId()).stream().map(Object::toString) // 假设返回的元素是字符串类型，如果不是，可以根据实际情况调整
                    .map(Long::parseLong).collect(Collectors.toList());
            // 获得要删除的文章点赞记录
            List<ArticleLikes> articleLikesFromDB = idToArticleLikesMap.get(article.getId());
            if (articleLikesFromDB == null)
            {
                articleLikesFromDB = new ArrayList<>();
            }
            /*如果redis的点赞用户集合为空，则不执行删除和添加，
                这种情况我们认为redis宕机然后刚刚重启
                并将数据库中的对应数据同步至redis中
             */
            if (articleLikedUserIds.isEmpty())
            {
                for (ArticleLikes likes : articleLikesFromDB)
                {
                    articleLikesService.addUserToLikeSet(article.getId(), likes.getUserId());
                }
                articleLikesService.setArticleLikes(article.getId(), articleLikesFromDB.size());
                continue;
            }
            // 比较数目和结合的size，使其一致，以集合size为准，并更新article对应记录的点赞数
            long articleLikeListSize = Long.parseLong(articleLikesFromRedis.toString());
            if (articleLikesFromRedis.equals(articleLikeListSize))
            {
                articleLikesService.setArticleLikes(article.getId(), articleLikeListSize);
            }
            if (!article.getSolutionLikes().equals(articleLikeListSize))
            {
                article.setSolutionLikes(articleLikeListSize);
                toUpdateSolution.add(article);
            }
            Iterator<ArticleLikes> iterator = articleLikesFromDB.iterator();
            while (iterator.hasNext())
            {
                ArticleLikes likes = iterator.next();
                if (!articleLikedUserIds.contains(likes.getUserId()))
                {
                    toDeleteArticleLikes.add(likes);
                }
            }
            // 获得要添加的文章点赞记录
            List<Long> collectUserIdFromDB = articleLikesFromDB.stream().map(ArticleLikes::getUserId).collect(Collectors.toList());
            for (Long articleLikedUserId : articleLikedUserIds)
            {
                if (!collectUserIdFromDB.contains(articleLikedUserId))
                {
                    ArticleLikes articleLikes1 = new ArticleLikes();
                    articleLikes1.setArticleId(article.getId());
                    articleLikes1.setUserId(articleLikedUserId);
                    toAddArticleLikes.add(articleLikes1);
                }
            }
        }
        // 更新question_solution表
        if (toUpdateSolution.size() > 0)
        {
            questionSolutionService.updateBatchById(toUpdateSolution);
        }
        // 更新article_likes表中的字段（删除和添加）
        if (toDeleteArticleLikes.size() > 0)
        {
            articleLikesService.removeByIds(toDeleteArticleLikes.stream().map(ArticleLikes::getId).collect(Collectors.toList()));
        }
        if (toAddArticleLikes.size() > 0)
        {
            articleLikesService.saveBatch(toAddArticleLikes);
        }
    }
}
