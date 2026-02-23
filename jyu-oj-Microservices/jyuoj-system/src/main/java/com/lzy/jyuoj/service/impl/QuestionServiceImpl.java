package sspu.zzx.sspuoj.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.constant.CommonConstant;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.exception.ThrowUtils;
import sspu.zzx.sspuoj.mapper.QuestionMapper;
import sspu.zzx.sspuoj.model.dto.question.QuestionQueryRequest;
import sspu.zzx.sspuoj.model.entity.Question;
import sspu.zzx.sspuoj.model.entity.User;
import sspu.zzx.sspuoj.model.vo.question.QuestionVO;
import sspu.zzx.sspuoj.model.vo.user.UserVO;
import sspu.zzx.sspuoj.service.QuestionService;
import sspu.zzx.sspuoj.service.UserService;
import sspu.zzx.sspuoj.service.impl.sys.StpInterfaceImpl;
import sspu.zzx.sspuoj.utils.SqlUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static sspu.zzx.sspuoj.constant.UserConstant.ADMIN_ROLE;

/**
 * @author ZZX
 * @description 针对表【question(题目)】的数据库操作Service实现
 * @createDate 2023-08-07 20:58:00
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService
{


    @Resource
    private UserService userService;

    /**
     * 校验题目是否合法
     *
     * @param question
     * @param add
     */
    @Override
    public void validQuestion(Question question, boolean add)
    {
        if (question == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();
        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();
        String questionType = question.getQuestionType();
        // 创建时，参数不能为空
        if (add)
        {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags, questionType), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
        if (StringUtils.isNotBlank(answer) && answer.length() > 8192)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "答案过长");
        }
        if (StringUtils.isNotBlank(judgeCase) && judgeCase.length() > 8192)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题用例过长");
        }
        if (StringUtils.isNotBlank(judgeConfig) && judgeConfig.length() > 8192)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题配置过长");
        }
    }

    /**
     * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象，得到 mybatis 框架支持的查询 QueryWrapper 类）
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest)
    {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null)
        {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        List<String> tags = questionQueryRequest.getTags();
        String answer = questionQueryRequest.getAnswer();
        Long userId = questionQueryRequest.getUserId();
        String questionType = questionQueryRequest.getQuestionType();
        String questionDifficulty = questionQueryRequest.getQuestionDifficulty();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);
        if (CollectionUtils.isNotEmpty(tags))
        {
            for (String tag : tags)
            {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionQueryRequest.getIsPrivate()), "isPrivate", questionQueryRequest.getIsPrivate());
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(StringUtils.isNotBlank(questionType), "questionType", questionType);
        queryWrapper.eq(StringUtils.isNotBlank(questionDifficulty), "questionDifficulty", questionDifficulty);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request)
    {
        QuestionVO questionVO = QuestionVO.objToVo(question);
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0)
        {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUserVO(userVO);
        return questionVO;
    }

    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request)
    {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollectionUtils.isEmpty(questionList))
        {
            return questionVOPage;
        }
        questionVOPage.setRecords(getQuestionVOList(questionList, true));
        return questionVOPage;
    }

    @Override
    public List<QuestionVO> getQuestionVOList(List<Question> questionList, Boolean containUsers)
    {
        // todo 关联查询用户信息，后续可将此处写入简历：避免循环中调用sql语句
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 填充信息
        List<QuestionVO> questionVOList = questionList.stream().map(question ->
        {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            if (containUsers)
            {
                Long userId = question.getUserId();
                User user = null;
                if (userIdUserListMap.containsKey(userId))
                {
                    user = userIdUserListMap.get(userId).get(0);
                }
                questionVO.setUserVO(userService.getUserVO(user));
            }
            return questionVO;
        }).collect(Collectors.toList());

        return questionVOList;
    }


}




