package sspu.zzx.sspuoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.mapper.ArticleLikesMapper;
import sspu.zzx.sspuoj.model.entity.ArticleLikes;
import sspu.zzx.sspuoj.service.ArticleLikesService;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ZZX
 * @description 针对表【article_likes】的数据库操作Service实现
 * @createDate 2023-12-12 15:14:24
 */
@Service
public class ArticleLikesServiceImpl extends ServiceImpl<ArticleLikesMapper, ArticleLikes> implements ArticleLikesService
{
    private final RedisTemplate<String, Object> redisTemplate;
    private final SetOperations<String, Object> setOperations;
    private final HashOperations<String, Object, Object> hashOperations;

    @Autowired
    public ArticleLikesServiceImpl(RedisTemplate<String, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
        this.setOperations = redisTemplate.opsForSet();
        this.hashOperations = redisTemplate.opsForHash();
    }

    // 添加用户到文章的点赞集合中，同时设置集合键永不过期
    public void addUserToLikeSet(Long articleId, Long userId)
    {
        Long add = setOperations.add(getArticleLikeSetKey(articleId), userId);
        // 设置集合键150年过期
        redisTemplate.expire(getArticleLikeSetKey(articleId), 365 * 150, TimeUnit.DAYS);
    }


    // 检查用户是否已经点赞
    public boolean isUserLiked(Long articleId, Long userId)
    {
        return Boolean.TRUE.equals(setOperations.isMember(getArticleLikeSetKey(articleId), userId));
    }

    // 设置文章的点赞数
    public void setArticleLikes(Long articleId, long likes)
    {
        hashOperations.put(getArticleLikesHashKey(), articleId, likes);
    }

    // 获取文章的点赞数
    public Long getArticleLikes(Long articleId)
    {
        Object likes = hashOperations.get(getArticleLikesHashKey(), articleId);
        return likes != null ? Long.parseLong(likes.toString()) : 0L;
    }

    // 获取文章点赞的用户ID集合
    public Set<Object> getArticleLikedUsers(Long articleId)
    {
        Set<Object> likedUsers = setOperations.members(getArticleLikeSetKey(articleId));
        return likedUsers != null ? likedUsers : Collections.emptySet();
    }


    // 移除用户从文章的点赞集合中
    public void removeUserFromLikeSet(Long articleId, Long userId)
    {
        setOperations.remove(getArticleLikeSetKey(articleId), userId);
    }

    // 获取文章的点赞集合的键
    private String getArticleLikeSetKey(Long articleId)
    {
        return "article:" + articleId + ":likes";
    }

    // 获取文章点赞数的哈希表键
    private String getArticleLikesHashKey()
    {
        return "article:likes";
    }

    // 点赞
    public void like(Long articleId, Long userId)
    {
        addUserToLikeSet(articleId, userId);
        Long likes = getArticleLikes(articleId);
        if (likes >= 0)
        {
            setArticleLikes(articleId, likes + 1);
        }
    }

    // 取消点赞
    public void cancelLike(Long articleId, Long userId)
    {
        if (isUserLiked(articleId, userId))
        {
            removeUserFromLikeSet(articleId, userId);
            Long likes = getArticleLikes(articleId);
            if (likes > 0)
            {
                setArticleLikes(articleId, likes - 1);
            }
        }
    }

    @Override
    public Long getArticleLikesCount(Long articleId)
    {
        return null;
    }

    @Override
    public Boolean likeArticleOrNot(Long articleId, Long userId)
    {
        // 获得当前点赞文章的用户集合
        Set<Object> likeUsers = getArticleLikedUsers(articleId);
        // 如果存在该用户，就取消点赞
        if (likeUsers.size() > 0 && likeUsers.contains(userId))
        {
            cancelLike(articleId, userId);
            return false;
        }
        // 反之，点赞
        else
        {
            like(articleId, userId);
            return true;
        }
    }

    @Override
    public Boolean ifLiked(Long articleId, Long userId)
    {
        // 首先从redis检查，如果有，那么数据库里面最终也一定会有
        Set<Long> articleLikedUsers = getArticleLikedUsers(articleId).stream().map(e -> (Long) e).collect(Collectors.toSet());
        if (articleLikedUsers.contains(userId)) return true;
        /*
        这块感觉不用，保证实时性比较好，redis宕机后再同步就好了
        // 如果redis中没有，则从数据库中查，有则有，否则那确实是没有
        QueryWrapper<ArticleLikes> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("article_id", articleId);
        queryWrapper.eq("user_id", userId);
        List<ArticleLikes> list = this.list(queryWrapper);
        return !list.isEmpty();
         */
        return false;
    }
}




