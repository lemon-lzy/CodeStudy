package sspu.zzx.sspuoj.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import sspu.zzx.sspuoj.aop.annotation.OpLog;
import sspu.zzx.sspuoj.common.BaseResponse;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.common.ResultUtils;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.exception.ThrowUtils;
import sspu.zzx.sspuoj.model.dto.article.ArticleCommentQueryRequest;
import sspu.zzx.sspuoj.model.dto.questionsolution.QuestionSolutionAddRequest;
import sspu.zzx.sspuoj.model.dto.questionsolution.QuestionSolutionQueryRequest;
import sspu.zzx.sspuoj.model.entity.*;
import sspu.zzx.sspuoj.model.vo.question.QuestionSolutionVo;
import sspu.zzx.sspuoj.service.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/12/4 10:23
 */
@RestController
@RequestMapping("/article")
@Slf4j
public class ArticleMicroController
{
    @Resource
    private QuestionSolutionService questionSolutionService;
    @Resource
    private ArticleCommentService articleCommentService;
    @Resource
    private ArticleLikesService articleLikesService;
    @Resource
    private UserService userService;
    @Resource
    private QuestionService questionService;
    @Resource
    private SysConfigService sysConfigService;


    /*题解-QuestionSolutionController*/

    /**
     * 查找所有题解类别（封装类）
     *
     * @param questionSolutionQueryRequest
     * @return
     */
    @SaIgnore
    @PostMapping("/question/solution/list/vo")
    public BaseResponse<List<QuestionSolutionVo>> listQuestionSolutionVOByPage(@RequestBody QuestionSolutionQueryRequest questionSolutionQueryRequest)
    {
        List<QuestionSolution> records = questionSolutionService.list(questionSolutionService.getQueryWrapper(questionSolutionQueryRequest));
        List<QuestionSolutionVo> collect = records.stream().map(questionSolution ->
        {
            QuestionSolutionVo questionSolutionVo = new QuestionSolutionVo();
            BeanUtils.copyProperties(questionSolution, questionSolutionVo);
            return questionSolutionVo;
        }).collect(Collectors.toList());
        return ResultUtils.success(collect);
    }

    /**
     * 根据id获取题解
     *
     * @param id
     * @return
     */
    @GetMapping("/question/solution/getSolutionById/{id}")
    public BaseResponse<QuestionSolution> getQuestionSolutionById(@PathVariable("id") Long id)
    {
        if (id == null || id <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionSolution solution = questionSolutionService.getById(id);
        // 异步更新浏览数
        Thread thread = new Thread(() ->
        {
            solution.setSolutionViews(solution.getSolutionViews() + 1);
            questionSolutionService.updateById(solution);
        });
        thread.start();

        return ResultUtils.success(solution);
    }

    /**
     * 根据questionId获取官方题解
     *
     * @param questionId
     * @return
     */
    @GetMapping("/question/solution/getOfficialSolutionByQuestionId/{questionId}")
    public BaseResponse<QuestionSolution> getOfficialSolutionByQuestionId(@PathVariable Long questionId)
    {
        if (questionId == null || questionId <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id不存在！");
        }
        QueryWrapper<QuestionSolution> wrapper = new QueryWrapper<>();
        wrapper.eq("questionId", questionId);
        wrapper.eq("title", "官方题解");
        QuestionSolution solution = questionSolutionService.getOne(wrapper);

        return ResultUtils.success(solution);
    }


    /**
     * 创建题解
     *
     * @param questionSolutionAddRequest
     * @return
     */
    @OpLog("题解创建:question_solution")
    @PostMapping("/question/solution/add")
    public BaseResponse<Long> addQuestionSolution(@RequestBody QuestionSolutionAddRequest questionSolutionAddRequest)
    {
        if (questionSolutionAddRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 数据校验
        if (StringUtils.isAnyBlank(questionSolutionAddRequest.getType(), questionSolutionAddRequest.getTitle(), questionSolutionAddRequest.getIntroduction()) || ("answer".equals(questionSolutionAddRequest.getType()) && questionSolutionAddRequest.getQuestionId() <= 0))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是官方题解，那么只有管理员或出题者可以写
        if ("answer".equals(questionSolutionAddRequest.getType()))
        {
            if ("官方题解".equals(questionSolutionAddRequest.getTitle()))
            {
                if (!userService.isAdmin((HttpServletRequest) null))
                {
                    Question question = questionService.getById(questionSolutionAddRequest.getQuestionId());
                    if (!question.getUserId().equals(StpUtil.getLoginIdAsLong()))
                    {
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有管理员或出题者可以写官方题解！");
                    }
                }
            }
        }
        // 初步复制题解基本信息
        QuestionSolution questionSolution = new QuestionSolution();
        BeanUtils.copyProperties(questionSolutionAddRequest, questionSolution);
        // 进一步复制创建者信息，如果为外部图文，作者名无需系统定义，由用户输入即可
        if (StringUtils.isBlank(questionSolutionAddRequest.getAuthorName()))
        {
            User user = userService.getById(StpUtil.getLoginIdAsLong());
            questionSolution.setUserId(user.getId());
            questionSolution.setAuthorName(user.getUserName());
            questionSolution.setAuthorAvatar(user.getUserAvatar());
        }
        boolean result = questionSolutionService.save(questionSolution);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionSolutionId = questionSolution.getId();
        // 异步更新分享地址
        Thread thread = new Thread(() ->
        {
            QueryWrapper<SysConfig> wrapper = new QueryWrapper<>();
            wrapper.eq("configKey","solution_prefix");
            SysConfig sysConfig = sysConfigService.getOne(wrapper);
            String configValue = sysConfig.getConfigValue();
            JSONObject jsonObject = JSON.parseObject(configValue);
            String value = jsonObject.getString("value");
            String url = value + "/" + newQuestionSolutionId;
            questionSolution.setSharingLink(url);
            questionSolutionService.updateById(questionSolution);
        });
        thread.start();
        return ResultUtils.success(newQuestionSolutionId);
    }


    /**
     * 更新题解（包括日报、通知和题解）
     *
     * @param questionSolution
     * @return
     */
    @OpLog("题解更新:question_solution")
    @PutMapping("/question/solution/update")
    public BaseResponse<Boolean> updateQuestionSolution(@RequestBody QuestionSolution questionSolution)
    {
        if (questionSolution == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = questionSolutionService.updateById(questionSolution);
        return ResultUtils.success(result);
    }


    /**
     * 根据id删除记录
     *
     * @param id
     * @return
     */
    @OpLog("题解删除:question_solution")
    @DeleteMapping("/question/solution/delete")
    public BaseResponse<Boolean> delete(@RequestParam Long id)
    {
        if (id == null || id <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 确保管理员或本人才能删除
        if (!userService.isAdmin((HttpServletRequest) null))
        {
            QuestionSolution questionSolution = questionSolutionService.getById(id);
            if (!questionSolution.getUserId().equals(StpUtil.getLoginIdAsLong()))
            {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有管理员或本人才能删除！");
            }
        }
        boolean result = questionSolutionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }


    /*文章评论：ArticleCommentController*/

    /**
     * 查询文章评论列表
     *
     * @param articleCommentQueryRequest
     * @return
     */
    @PostMapping("/article/comment/list")
    public BaseResponse<List<ArticleComment>> listAll(@RequestBody ArticleCommentQueryRequest articleCommentQueryRequest)
    {
        if (articleCommentQueryRequest.getArticleId() == null)
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR);
        }
        // 获得初始list
        List<ArticleComment> articleComments = articleCommentService.list(articleCommentService.getQueryWrapper(articleCommentQueryRequest));
        // 判断是否聚合（嵌套）
        return articleCommentQueryRequest.getIfMerge() ? ResultUtils.success(mergeComments(articleComments)) : ResultUtils.success(articleComments);
    }

    /**
     * 新增评论或回复
     *
     * @param articleComment
     * @return
     */
    @PostMapping("/article/comment/reply")
    public BaseResponse<Boolean> replyComment(@RequestBody ArticleComment articleComment)
    {
        BaseResponse<Boolean> response = ResultUtils.success(articleCommentService.reply(articleComment));
        response.setMessage("评论提交成功，将在审核通过后显示~");
        return response;
    }

    /**
     * 编辑评论（管理员或本人）
     *
     * @param articleComment
     * @return
     */
    @PutMapping("/article/comment/edit")
    public BaseResponse<Boolean> editComment(@RequestBody ArticleComment articleComment)
    {
        // 判空
        if (articleComment == null || ObjectUtils.isEmpty(articleComment.getId()))
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR);
        }
        // 判断是否是管理员或本人
        if (!userService.ifAdminOrSelf(articleComment.getSenderId()))
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "仅管理员或本人可以编辑此评论");
        }
        // 异步审核评论文本是否合规
        CompletableFuture.runAsync(() ->
        {
            Boolean aBoolean = articleCommentService.commentModeration(articleComment.getComment());
            if (aBoolean)
            {
                articleCommentService.updateById(articleComment);
            }
        });
        BaseResponse<Boolean> response = ResultUtils.success(true);
        response.setMessage("评论提交成功，将在审核通过后显示~");
        return response;
    }

    /**
     * 删除评论（管理员或本人）
     *
     * @param articleComment todo 级联批量删除
     * @return
     */
    @DeleteMapping("/article/comment/delete")
    public BaseResponse<Boolean> deleteComment(@RequestBody ArticleComment articleComment)
    {
        // 判空
        if (articleComment == null || ObjectUtils.isEmpty(articleComment.getId()))
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR);
        }
        // 判断是否是管理员或本人
        if (!userService.ifAdminOrSelf(articleComment.getSenderId()))
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "仅管理员或本人可以删除此评论");
        }
        List<ArticleComment> articleComments = new ArrayList<>();
        articleComments.add(articleComment);
        if (articleComment.getChildren() != null)
        {
            articleComments.addAll(articleComment.getChildren());
        }
        // 同时异步更新文章的点赞数
        CompletableFuture.runAsync(() ->
        {
            ArticleComment commentServiceById = articleCommentService.getById(articleComment.getId());
            QuestionSolution article = questionSolutionService.getById(commentServiceById.getArticleId());
            article.setSolutionComments(article.getSolutionComments() - articleComments.size());
            questionSolutionService.updateById(article);
        });
        return ResultUtils.success(articleCommentService.removeByIds(articleComments.stream().map(ArticleComment::getId).collect(Collectors.toList())));
    }


    // 聚合评论的方法
    private List<ArticleComment> mergeComments(List<ArticleComment> articleComments)
    {
        return articleComments.stream().filter(articleComment -> articleComment.getReceiverId() == null).map(parentComment ->
        {
            parentComment.setChildren(articleComments.stream().filter(childComment -> parentComment.getSenderId().equals(childComment.getReceiverId())).collect(Collectors.toList()));
            return parentComment;
        }).collect(Collectors.toList());
    }

    /*文章点赞：ArticleLikesController*/

    /**
     * 用户点赞/取消点赞文章
     *
     * @param articleId
     * @param userId
     * @return
     */
    @PostMapping("/article/likes/likeOrNot")
    public BaseResponse<Boolean> likeArticleOrNot(@RequestParam("articleId") Long articleId, @RequestParam("userId") Long userId)
    {
        if (ObjectUtils.isEmpty(articleId) || ObjectUtils.isEmpty(userId))
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR);
        }

        return ResultUtils.success(articleLikesService.likeArticleOrNot(articleId, userId));
    }

    /**
     * 判断用户是否点赞了文章
     *
     * @param articleId
     * @param userId
     * @return
     */
    @GetMapping("/article/likes/ifLiked")
    public BaseResponse<Boolean> ifLiked(@RequestParam("articleId") Long articleId, @RequestParam("userId") Long userId)
    {
        if (ObjectUtils.isEmpty(articleId) || ObjectUtils.isEmpty(userId))
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR);
        }

        return ResultUtils.success(articleLikesService.ifLiked(articleId, userId));
    }
}
