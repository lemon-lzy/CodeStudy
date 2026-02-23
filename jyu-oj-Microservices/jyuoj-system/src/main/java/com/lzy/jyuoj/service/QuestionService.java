package sspu.zzx.sspuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import sspu.zzx.sspuoj.model.dto.question.QuestionQueryRequest;
import sspu.zzx.sspuoj.model.entity.Question;
import sspu.zzx.sspuoj.model.vo.question.QuestionVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author ZZX
 * @description 针对表【question(题目)】的数据库操作Service
 * @createDate 2023-08-07 20:58:00
 */
public interface QuestionService extends IService<Question>
{


    /**
     * 校验
     *
     * @param question
     * @param add
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);

    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);

    /**
     * 根据题目信息列表获取封装了列表
     *
     * @param questionList
     * @param containUsers
     * @return
     */
    List<QuestionVO> getQuestionVOList(List<Question> questionList, Boolean containUsers);

}
