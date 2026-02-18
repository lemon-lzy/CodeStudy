package com.lzy.codestudybackend.mapper.question;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzy.codestudybackend.model.dto.questionlist.QuestionListIdToCount;
import com.lzy.codestudybackend.model.entity.question.Question;
import com.lzy.codestudybackend.model.entity.question.QuestionUnion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;


/**
 * @author ZZX
 * @description 针对表【question_union】的数据库操作Mapper
 * @createDate 2023-11-15 14:36:50
 * @Entity sspu.zzx.sspuoj.model.QuestionUnion
 */
@Mapper
public interface QuestionUnionMapper extends BaseMapper<QuestionUnion>
{
    /**
     * 根据题单id集合获取各题单包含的题目数量
     *
     * @param listIds
     * @return
     */
    List<QuestionListIdToCount> getQuestionCountByListIds(@Param("listIds") List<Long> listIds);

    /**
     * 根据题单id集合获取各题单包含的题目
     *
     * @param questionListId
     * @return
     */
    List<Question> getQuestionsById(@Param("questionListId") Long questionListId);
}




