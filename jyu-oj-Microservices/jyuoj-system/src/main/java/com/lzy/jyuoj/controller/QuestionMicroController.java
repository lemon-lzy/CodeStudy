package sspu.zzx.sspuoj.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import sspu.zzx.sspuoj.aop.annotation.OpLog;
import sspu.zzx.sspuoj.common.BaseResponse;
import sspu.zzx.sspuoj.common.DeleteRequest;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.common.ResultUtils;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.exception.ThrowUtils;
import sspu.zzx.sspuoj.model.dto.question.*;
import sspu.zzx.sspuoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import sspu.zzx.sspuoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import sspu.zzx.sspuoj.model.entity.Question;
import sspu.zzx.sspuoj.model.entity.QuestionSubmit;
import sspu.zzx.sspuoj.model.entity.SysConfig;
import sspu.zzx.sspuoj.model.entity.User;
import sspu.zzx.sspuoj.model.vo.question.QuestionSubmitVO;
import sspu.zzx.sspuoj.model.vo.question.QuestionVO;
import sspu.zzx.sspuoj.service.QuestionService;
import sspu.zzx.sspuoj.service.QuestionSubmitService;
import sspu.zzx.sspuoj.service.SysConfigService;
import sspu.zzx.sspuoj.service.UserService;
import sspu.zzx.sspuoj.service.impl.sys.StpInterfaceImpl;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static sspu.zzx.sspuoj.constant.UserConstant.ADMIN_ROLE;

/**
 * 题目接口
 *
 * @author ZZX
 * @from SSPU
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionMicroController
{

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private StpInterfaceImpl stpInterface;

    @Resource
    private SysConfigService sysConfigService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    private final static Gson GSON = new Gson();


    /*题目：QuestionController*/

    // region 增删改查

    /**
     * todo 创建
     *
     * @param questionAddRequest
     * @return
     */
    @OpLog("题目创建:question")
    @PostMapping("/add")
    @SaCheckPermission(value = {"create.question"}, orRole = "admin")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest)
    {
        if (questionAddRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        List<String> tags = questionAddRequest.getTags();
        if (tags != null)
        {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionAddRequest.getJudgeCase();
        if (judgeCase != null)
        {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        if (judgeConfig != null)
        {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        questionService.validQuestion(question, true);
        question.setUserId(StpUtil.getLoginIdAsLong());
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionId = question.getId();
        // 异步更新题目标签列表
        CompletableFuture.runAsync(() ->
        {
            if (tags != null)
            {
                updateKnowledgeListConfig(tags);
            }
        });
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除（仅创建者本人或管理员）
     *
     * @param deleteRequest
     * @return
     */
    @OpLog("题目删除:question")
    @PostMapping("/delete")
    @SaCheckPermission(value = {"create.question"}, orRole = "admin")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest)
    {
        if (deleteRequest == null || deleteRequest.getId() <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(StpUtil.getLoginIdAsLong());
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!(oldQuestion.getUserId().equals(user.getId()) || ADMIN_ROLE.equals(user.getUserType())))
        {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅创建者本人或管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    @OpLog("题目更新:question")
    @PostMapping("/update")
    @SaCheckPermission(value = {"create.question"}, orRole = "admin")
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest)
    {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null)
        {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionUpdateRequest.getJudgeCase();
        if (judgeCase != null)
        {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        if (judgeConfig != null)
        {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        long id = questionUpdateRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可修改
        User user = userService.getById(StpUtil.getLoginIdAsLong());
        if (!(oldQuestion.getUserId().equals(user.getId()) || ADMIN_ROLE.equals(user.getUserType())))
        {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = questionService.updateById(question);
        // 异步更新题目标签列表
        CompletableFuture.runAsync(() ->
        {
            if (tags != null)
            {
                updateKnowledgeListConfig(tags);
            }
        });
        return ResultUtils.success(result);
    }

    /**
     * 更新知识标签列表参数配置
     *
     * @param tags
     */
    public void updateKnowledgeListConfig(List<String> tags)
    {
        // 获取现有的知识库标签
        QueryWrapper<SysConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("configKey", "question_knowledge_list");
        SysConfig sysConfig = sysConfigService.getOne(wrapper);
        if (sysConfig == null)
        {
            return;
        }
        JSONObject jsonObject = JSON.parseObject(sysConfig.getConfigValue());
        JSONArray oldKnowledgeList = jsonObject.getJSONArray("knowledgeList");
        List<String> knowledgeList = oldKnowledgeList.stream().map(String::valueOf).collect(Collectors.toList());
        // 更新知识库标签
        if (tags != null)
        {
            for (String tag : tags)
            {
                if (!knowledgeList.contains(tag))
                {
                    knowledgeList.add(tag);
                }
            }
        }
        // 更新配置
        jsonObject.put("knowledgeList", knowledgeList);
        sysConfig.setConfigValue(jsonObject.toJSONString());
        sysConfigService.updateById(sysConfig);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Question> getQuestionById(long id, HttpServletRequest request)
    {
        if (id <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null)
        {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 不是本人或管理员，不能直接获取所有信息
        if (!question.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser))
        {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(question);
    }

    /**
     * 根据 id 获取题目（脱敏）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request)
    {
        if (id <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null)
        {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 根据 id 获取题目（脱敏）列表
     *
     * @param ids
     * @return
     */
    @GetMapping("/get/vo/by/ids")
    public BaseResponse<List<QuestionVO>> getQuestionVOByIds(@RequestParam List<Long> ids)
    {
        if (ids == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<Question> questions = new ArrayList<>();
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        questions.addAll(questionService.list(queryWrapper));
        return ResultUtils.success(questionService.getQuestionVOList(questions, false));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     * @description 只返回公开的题目
     */
    @SaIgnore
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest, HttpServletRequest request)
    {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        questionQueryRequest.setIsPrivate(false);
        Page<Question> questionPage = questionService.page(new Page<>(current, size), questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest, HttpServletRequest request)
    {
        if (questionQueryRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size), questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取题目列表（仅管理员和创建者本人）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckPermission(value = {"create.question"}, orRole = "admin")
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest)
    {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 获取用户角色列表
        Object loginIdDefaultNull = StpUtil.getLoginIdDefaultNull();
        if (ObjectUtils.isNotEmpty(loginIdDefaultNull))
        {
            Long loginId = Long.valueOf(loginIdDefaultNull.toString());
            List<String> roleList = stpInterface.getRoleList(loginId, "login");
            if (!roleList.contains(ADMIN_ROLE))
            {
                questionQueryRequest.setUserId(loginId);
            }
        }
        Page<Question> questionPage = questionService.page(new Page<>(current, size), questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    @SaCheckPermission(value = {"create.question"}, orRole = "admin")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request)
    {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<String> tags = questionEditRequest.getTags();
        if (tags != null)
        {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionEditRequest.getJudgeCase();
        if (judgeCase != null)
        {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionEditRequest.getJudgeConfig();
        if (judgeConfig != null)
        {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        User loginUser = userService.getLoginUser(request);
        long id = questionEditRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser))
        {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }

    /*题目提交：QuestionSubmitController*/

    /**
     * 提交题目
     * todo new
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交记录的 id
     */
    @OpLog("提交题目答案:question_submit")
    @PostMapping("/question_submit/do")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest, HttpServletRequest request)
    {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final User loginUser = userService.getLoginUser(request);
        long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(questionSubmitId);
    }

    /**
     * 分页获取题目提交列表
     * （除了管理员外，普通用户只能看到非答案、提交代码等公开信息）
     * 管理员可以看到所有人的提交
     * 其他人只能看到自己的
     * todo new
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/question_submit/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request)
    {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        final User loginUser = userService.getLoginUser(request);
        if (!userService.isAdmin(loginUser))
        {
            questionSubmitQueryRequest.setUserId(loginUser.getId());
        }
        // 从数据库中查询原始的题目提交分页信息
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size), questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        // 返回脱敏信息
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
    }

    /**
     * 根据id获得题目提交信息
     * todo new
     * @param id
     * @return
     */
    @GetMapping("/question_submit/{id}")
    public BaseResponse<QuestionSubmitVO> getQuestionSubmitById(@PathVariable Long id)
    {
        if (id == null || id <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id不能为空");
        }
        QuestionSubmit questionSubmit = questionSubmitService.getById(id);
        if (questionSubmit == null)
        {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到题目提交信息");
        }
        // 返回完整脱敏信息
        final User loginUser = userService.getLoginUser(null);

        return ResultUtils.success(questionSubmitService.getQuestionSubmitVO(questionSubmit, loginUser, true));
    }


}
