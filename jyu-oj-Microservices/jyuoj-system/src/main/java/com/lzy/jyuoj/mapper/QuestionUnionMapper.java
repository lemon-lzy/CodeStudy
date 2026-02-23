package sspu.zzx.sspuoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sspu.zzx.sspuoj.model.dto.questionlist.QuestionListIdToCount;
import sspu.zzx.sspuoj.model.entity.Question;
import sspu.zzx.sspuoj.model.entity.QuestionUnion;

import java.util.List;
import java.util.Map;


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




