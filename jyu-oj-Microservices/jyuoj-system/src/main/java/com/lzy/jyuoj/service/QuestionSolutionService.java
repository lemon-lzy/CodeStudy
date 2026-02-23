package sspu.zzx.sspuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import sspu.zzx.sspuoj.model.dto.questionsolution.QuestionSolutionQueryRequest;
import sspu.zzx.sspuoj.model.entity.QuestionSolution;


/**
 * @author ZZX
 * @description 针对表【question_solution】的数据库操作Service
 * @createDate 2023-11-15 14:36:45
 */
public interface QuestionSolutionService extends IService<QuestionSolution>
{

    QueryWrapper<QuestionSolution> getQueryWrapper(QuestionSolutionQueryRequest questionSolutionQueryRequest);
}
