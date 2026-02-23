package sspu.zzx.sspuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.constant.CommonConstant;
import sspu.zzx.sspuoj.mapper.QuestionSolutionMapper;
import sspu.zzx.sspuoj.model.dto.questionsolution.QuestionSolutionQueryRequest;
import sspu.zzx.sspuoj.model.entity.QuestionSolution;
import sspu.zzx.sspuoj.service.QuestionSolutionService;
import sspu.zzx.sspuoj.utils.SqlUtils;

/**
 * @author ZZX
 * @description 针对表【question_solution】的数据库操作Service实现
 * @createDate 2023-11-15 14:36:45
 */
@Service
public class QuestionSolutionServiceImpl extends ServiceImpl<QuestionSolutionMapper, QuestionSolution> implements QuestionSolutionService
{

    @Override
    public QueryWrapper<QuestionSolution> getQueryWrapper(QuestionSolutionQueryRequest questionSolutionQueryRequest)
    {
        QueryWrapper<QuestionSolution> queryWrapper = new QueryWrapper<>();
        if (questionSolutionQueryRequest == null)
        {
            return queryWrapper;
        }
        Long id = questionSolutionQueryRequest.getId();
        String title = questionSolutionQueryRequest.getTitle();
        Long userId = questionSolutionQueryRequest.getUserId();
        Long questionId = questionSolutionQueryRequest.getQuestionId();
        String authorName = questionSolutionQueryRequest.getAuthorName();
        String type = questionSolutionQueryRequest.getType();
        String sortField = questionSolutionQueryRequest.getSortField();
        String sortOrder = questionSolutionQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(authorName), "authorName", authorName);
        queryWrapper.eq(StringUtils.isNotBlank(type), "type", type);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }
}




