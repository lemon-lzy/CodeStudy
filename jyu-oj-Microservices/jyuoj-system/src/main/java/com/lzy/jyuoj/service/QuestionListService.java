package sspu.zzx.sspuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import sspu.zzx.sspuoj.model.dto.questionlist.QuestionListAddRequest;
import sspu.zzx.sspuoj.model.dto.questionlist.QuestionListQueryRequest;
import sspu.zzx.sspuoj.model.entity.QuestionList;
import sspu.zzx.sspuoj.model.vo.question.QuestionListVo;
import sspu.zzx.sspuoj.model.vo.question.QuestionVO;

import java.util.List;


/**
 * @author ZZX
 * @description 针对表【question_list】的数据库操作Service
 * @createDate 2023-11-15 14:36:40
 */
public interface QuestionListService extends IService<QuestionList>
{

    /**
     * 分页获取题单信息
     *
     * @param questionListPage
     * @return
     */
    Page<QuestionListVo> getQuestionListVOPage(Page<QuestionList> questionListPage);

    /**
     * 获取题单信息列表(脱敏后）
     *
     * @param questionLists
     * @return
     */
    List<QuestionListVo> getQuestionListVOList(List<QuestionList> questionLists);

    /**
     * 获取查询条件
     *
     * @param questionListQueryRequest
     * @return
     */
    QueryWrapper<QuestionList> getQueryWrapper(QuestionListQueryRequest questionListQueryRequest);

    /**
     * 获取题单题目列表
     *
     * @param questionListId
     * @return
     */
    List<QuestionVO> getQuestionVOList(Long questionListId);

    /**
     * 添加题单
     *
     * @param questionListAddRequest
     * @return
     */
    Boolean addQuestionList(QuestionListAddRequest questionListAddRequest);

    /**
     * 修改题单
     *
     * @param questionListAddRequest
     * @return
     */
    Boolean editQuestionList(QuestionListAddRequest questionListAddRequest);
}
