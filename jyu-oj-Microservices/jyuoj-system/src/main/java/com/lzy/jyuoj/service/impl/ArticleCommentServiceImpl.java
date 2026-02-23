package sspu.zzx.sspuoj.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.constant.CommonConstant;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.mapper.ArticleCommentMapper;
import sspu.zzx.sspuoj.mapper.QuestionSolutionMapper;
import sspu.zzx.sspuoj.mapper.UserMapper;
import sspu.zzx.sspuoj.model.dto.article.ArticleCommentQueryRequest;
import sspu.zzx.sspuoj.model.entity.ArticleComment;
import sspu.zzx.sspuoj.model.entity.QuestionSolution;
import sspu.zzx.sspuoj.model.entity.User;
import sspu.zzx.sspuoj.service.ArticleCommentService;
import sspu.zzx.sspuoj.utils.SqlUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ZZX
 * @description 针对表【article_comment】的数据库操作Service实现
 * @createDate 2023-12-15 10:25:32
 */
@Service
@Slf4j
public class ArticleCommentServiceImpl extends ServiceImpl<ArticleCommentMapper, ArticleComment> implements ArticleCommentService
{
    @Resource
    private ArticleCommentMapper articleCommentMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private QuestionSolutionMapper questionSolutionMapper;

    @Value("${third-api.comment.url}")
    private String commentUrl;

    @Value("${third-api.comment.mode}")
    private String commentMode;

    @Override
    public QueryWrapper<ArticleComment> getQueryWrapper(ArticleCommentQueryRequest articleCommentQueryRequest)
    {
        QueryWrapper<ArticleComment> queryWrapper = new QueryWrapper<>();
        if (articleCommentQueryRequest == null)
        {
            return queryWrapper;
        }
        Long id = articleCommentQueryRequest.getId();
        Long articleId = articleCommentQueryRequest.getArticleId();
        Long senderId = articleCommentQueryRequest.getSenderId();
        Long receiverId = articleCommentQueryRequest.getReceiverId();
        String comment = articleCommentQueryRequest.getComment();
        String senderName = articleCommentQueryRequest.getSenderName();
        String sortField = articleCommentQueryRequest.getSortField();
        String sortOrder = articleCommentQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(comment), "comment", comment);
        queryWrapper.like(StringUtils.isNotBlank(senderName), "senderName", senderName);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(articleId), "articleId", articleId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(senderId), "senderId", senderId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(receiverId), "receiverId", receiverId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);

        return queryWrapper;
    }

    @Override
    public Boolean reply(ArticleComment articleComment)
    {
        if (articleComment == null)
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR);
        }
        // 文章id、作者id、评论内容至少不得为空
        if (ObjectUtils.isEmpty(articleComment.getArticleId()) || ObjectUtils.isEmpty(articleComment.getSenderId()) || StringUtils.isBlank(articleComment.getComment()))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检查是否已经评论过
        if (ObjectUtils.isEmpty(articleComment.getReceiverId()))
        {
            QueryWrapper<ArticleComment> wrapper = new QueryWrapper<>();
            wrapper.eq("articleId", articleComment.getArticleId());
            wrapper.eq("senderId", articleComment.getSenderId());
            wrapper.isNull("receiverId");
            ArticleComment comment = articleCommentMapper.selectOne(wrapper);
            if (comment != null)
            {
                throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "您已经评论过该文章，不能重复评论!");
            }
        }
        if (StringUtils.isAnyBlank(articleComment.getSenderName(), articleComment.getSenderAvatar()))
        {
            User user = userMapper.selectById(articleComment.getSenderId());
            articleComment.setSenderName(user.getUserName());
            articleComment.setSenderAvatar(user.getUserAvatar());
        }
        // 异步审核评论文本是否合规
        CompletableFuture.runAsync(() ->
        {
            Boolean aBoolean = commentModeration(articleComment.getComment());
            if (aBoolean)
            {
                // 正式添加
                int insert = articleCommentMapper.insert(articleComment);
                Boolean result = insert > 0;
                if (result)
                {
                    // 如果成功，则更新文章评论数
                    QuestionSolution article = questionSolutionMapper.selectById(articleComment.getArticleId());
                    UpdateWrapper<QuestionSolution> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id", articleComment.getArticleId()).set("solutionComments", article.getSolutionComments() + 1);
                    questionSolutionMapper.update(null, updateWrapper);
                }
            }
        });

        return true;
    }

    @Override
    @Retryable(
            value = {RestClientException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Boolean commentModeration(String comment)
    {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("comments", Arrays.asList(comment));
        paramMap.put("mode", commentMode);
        log.info("请求评论审核接口，URL：{}，参数：{}", commentUrl, paramMap);
        String result = HttpUtil.post(commentUrl, JSONUtil.toJsonStr(paramMap));
        log.info("请求评论审核接口，URL：{}，返回结果：{}", commentUrl, result);
        JSONObject resultObj = JSONUtil.parseObj(result);
        String results = JSONUtil.toJsonStr(resultObj.getObj("results"));
        // 定义正则表达式
        String regex = ".*\"label\":(\\d+).*";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);
        // 创建Matcher对象
        Matcher matcher = pattern.matcher(results);
        // 查找匹配
        if (matcher.matches())
        {
            // 获取匹配到的数字
            String labelValue = matcher.group(1);
            // 0 表示合法
            return "0".equals(labelValue);
        } else
        {
            return false;
        }
    }

}




