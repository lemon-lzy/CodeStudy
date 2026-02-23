package sspu.zzx.sspuoj.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.constant.CommonConstant;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.judge.JudgeService;
import sspu.zzx.sspuoj.mapper.QuestionSubmitMapper;
import sspu.zzx.sspuoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import sspu.zzx.sspuoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import sspu.zzx.sspuoj.model.entity.Question;
import sspu.zzx.sspuoj.model.entity.QuestionSubmit;
import sspu.zzx.sspuoj.model.entity.User;
import sspu.zzx.sspuoj.model.enums.JudgeInfoMessageEnum;
import sspu.zzx.sspuoj.model.enums.QuestionSubmitLanguageEnum;
import sspu.zzx.sspuoj.model.enums.QuestionSubmitStatusEnum;
import sspu.zzx.sspuoj.model.vo.question.QuestionSubmitVO;
import sspu.zzx.sspuoj.model.vo.question.QuestionVO;
import sspu.zzx.sspuoj.service.QuestionService;
import sspu.zzx.sspuoj.service.QuestionSubmitService;
import sspu.zzx.sspuoj.service.UserService;
import sspu.zzx.sspuoj.utils.SqlUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author ZZX
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2023-08-07 20:58:53
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit> implements QuestionSubmitService
{

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private JudgeService judgeService;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser)
    {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null)
        {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已提交题目
        long userId = loginUser.getId();
        // 检查是否存在该用户提交该题目但正在判题的记录
        QueryWrapper<QuestionSubmit> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        wrapper.eq("questionId", questionId);
        wrapper.eq("status", QuestionSubmitStatusEnum.WAITING.getValue());
        QuestionSubmit waitingQuestionSubmit = this.getOne(wrapper);
        if (waitingQuestionSubmit != null)
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "题目正在判题中，请勿重复提交！");
        }
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        if (!save)
        {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        Long questionSubmitId = questionSubmit.getId();
        // 异步执行判题服务
        CompletableFuture.runAsync(() ->
        {
            judgeService.doJudge(questionSubmitId);
            // 同时更新题目的通过数和通过总数
            question.setSubmitNum(question.getSubmitNum() + 1);
            QuestionSubmit resQuestionSubmit = this.getById(questionSubmitId);
            String judgeInfo = resQuestionSubmit.getJudgeInfo();
            JSONObject jsonObject = JSON.parseObject(judgeInfo);
            if (JudgeInfoMessageEnum.ACCEPTED.getValue().equals(jsonObject.getString("message")))
            {
                question.setAcceptedNum(question.getAcceptedNum() + 1);
            }
            this.questionService.updateById(question);
        });
        return questionSubmitId;
    }


    /**
     * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象，得到 mybatis 框架支持的查询 QueryWrapper 类）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest)
    {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null)
        {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser, Boolean isOnce)
    {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 脱敏：仅本人和管理员能看见自己（提交 userId 和登录用户 id 不同）提交的代码
        long userId = loginUser.getId();
        // 处理脱敏
        if (userId != questionSubmit.getUserId() && !userService.isAdmin(loginUser))
        {
            questionSubmitVO.setCode(null);
        }
        if (isOnce)
        {
            // 更新题目信息
            Long questionId = questionSubmit.getQuestionId();
            QuestionVO questionVO = questionService.getQuestionVO(questionService.getById(questionId), null);
            questionSubmitVO.setQuestionVO(questionVO);
            // 更新做题者信息
            User user = userService.getById(questionSubmit.getUserId());
            questionSubmitVO.setUserVO(userService.getUserVO(user));
        }

        return questionSubmitVO;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser)
    {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList))
        {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser, false)).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }


}




