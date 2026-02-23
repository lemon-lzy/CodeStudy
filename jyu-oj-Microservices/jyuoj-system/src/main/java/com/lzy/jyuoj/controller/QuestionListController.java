package sspu.zzx.sspuoj.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import sspu.zzx.sspuoj.aop.annotation.OpLog;
import sspu.zzx.sspuoj.common.BaseResponse;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.common.ResultUtils;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.exception.ThrowUtils;
import sspu.zzx.sspuoj.model.dto.questionlist.QuestionListAddRequest;
import sspu.zzx.sspuoj.model.dto.questionlist.QuestionListQueryRequest;
import sspu.zzx.sspuoj.model.entity.Question;
import sspu.zzx.sspuoj.model.entity.QuestionList;
import sspu.zzx.sspuoj.model.entity.QuestionUnion;
import sspu.zzx.sspuoj.model.entity.User;
import sspu.zzx.sspuoj.model.vo.question.QuestionListVo;
import sspu.zzx.sspuoj.model.vo.question.QuestionVO;
import sspu.zzx.sspuoj.service.QuestionListService;
import sspu.zzx.sspuoj.service.QuestionService;
import sspu.zzx.sspuoj.service.QuestionUnionService;
import sspu.zzx.sspuoj.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/12/25 10:55
 */
@RestController
@RequestMapping("/questionlist")
@Slf4j
public class QuestionListController
{
    @Resource
    private QuestionListService questionListService;
    @Resource
    private QuestionUnionService questionUnionService;
    @Resource
    private QuestionService questionService;
    @Resource
    private UserService userService;

    /**
     * 分页查询题单信息
     *
     * @param questionListQueryRequest
     * @return
     */
    @PostMapping("list/page/vo")
    public BaseResponse<Page<QuestionListVo>> questionListVoByPage(@RequestBody QuestionListQueryRequest questionListQueryRequest)
    {
        long current = questionListQueryRequest.getCurrent();
        long size = questionListQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<QuestionList> questionListPage = questionListService.page(new Page<>(current, size), questionListService.getQueryWrapper(questionListQueryRequest));
        return ResultUtils.success(questionListService.getQuestionListVOPage(questionListPage));
    }

    /**
     * 分页获取题单列表（仅管理员和创建者本人）
     *
     * @param questionListQueryRequest
     * @return
     */
    @PostMapping("list/page")
    @SaCheckPermission(value = {"create.questionList"}, orRole = "admin")
    public BaseResponse<Page<QuestionListVo>> questionListByPage(@RequestBody QuestionListQueryRequest questionListQueryRequest)
    {
        long current = questionListQueryRequest.getCurrent();
        long size = questionListQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(null);
        if (userService.isAdmin(loginUser))
        {
            questionListQueryRequest.setCreatorId(null);
        }
        else
        {
            questionListQueryRequest.setCreatorId(loginUser.getId());
        }
        Page<QuestionList> questionListPage = questionListService.page(new Page<>(current, size), questionListService.getQueryWrapper(questionListQueryRequest));
        return ResultUtils.success(questionListService.getQuestionListVOPage(questionListPage));
    }

    /**
     * 根据id获取题单信息
     *
     * @param questionListId
     * @return
     */
    @PostMapping("/getQuestionListById")
    public BaseResponse<QuestionList> getQuestionList(@RequestParam("questionListId") long questionListId)
    {
        return ResultUtils.success(questionListService.getById(questionListId));
    }


    /**
     * 根据题单id获取题目列表
     *
     * @param questionListId
     * @return
     */
    @GetMapping("/getQuestions/{questionListId}")
    public BaseResponse<List<QuestionVO>> getQuestionsById(@PathVariable Long questionListId)
    {
        if (ObjectUtils.isEmpty(questionListId))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(questionListService.getQuestionVOList(questionListId));
    }

    /**
     * 获取当前用户可以作为题单的题目合集
     *
     * @return
     */
    @GetMapping("/getAvailableQuestions")
    public BaseResponse<List<QuestionVO>> getAvailableQuestions()
    {
        // 获取用户是否是管理员，如果是可以获取全部题目
        boolean isAdmin = userService.isAdmin((HttpServletRequest) null);
        if (isAdmin)
        {
            return ResultUtils.success(questionService.getQuestionVOList(questionService.list(), false));
        }
        else
        {
            // 获取当前用户id
            long currentUserId = StpUtil.getLoginIdAsLong();
            // 获取公开或本人开发的题目
            QueryWrapper<Question> wrapper = new QueryWrapper<>();
            wrapper.eq("userId", currentUserId).or().eq("isPrivate", false);

            return ResultUtils.success(questionService.getQuestionVOList(questionService.list(wrapper), false));
        }
    }


    /**
     * 新建题单
     *
     * @param questionListAddRequest
     * @return
     */
    @PutMapping("add")
    @OpLog("新增题单:question_list")
    @SaCheckPermission(value = {"create.questionList"}, orRole = "admin")
    public BaseResponse<Boolean> addQuestionList(@RequestBody QuestionListAddRequest questionListAddRequest)
    {
        if (questionListAddRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(questionListService.addQuestionList(questionListAddRequest));
    }


    /**
     * 修改题单信息
     *
     * @param questionListAddRequest
     * @return
     */
    @PostMapping("edit")
    @OpLog("编辑题单:question_list")
    @SaCheckPermission(value = {"create.questionList"}, orRole = "admin")
    public BaseResponse<Boolean> editQuestionList(@RequestBody QuestionListAddRequest questionListAddRequest)
    {
        if (questionListAddRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(questionListService.editQuestionList(questionListAddRequest));
    }

    /**
     * 根据id删除题单信息
     *
     * @param questionListId
     * @return
     */
    @OpLog("删除题单:question_list")
    @DeleteMapping("/delete/{questionListId}")
    @SaCheckPermission(value = {"create.questionList"}, orRole = "admin")
    public BaseResponse<Boolean> deleteQuestionListById(@PathVariable Long questionListId)
    {
        if (ObjectUtils.isEmpty(questionListId))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 首先删除题单信息
        boolean remove = questionListService.removeById(questionListId);
        // 如果成功则异步删除题单的题目绑定信息
        if (remove)
        {
            QueryWrapper<QuestionUnion> wrapper = new QueryWrapper<>();
            wrapper.eq("listId", questionListId);
            remove = questionUnionService.remove(wrapper);
        }
        return ResultUtils.success(remove);
    }
}
