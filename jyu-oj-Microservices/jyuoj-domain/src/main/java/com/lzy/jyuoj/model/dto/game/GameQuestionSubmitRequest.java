package sspu.zzx.sspuoj.model.dto.game;

import lombok.Data;
import sspu.zzx.sspuoj.model.dto.questionsubmit.QuestionSubmitAddRequest;

import java.util.List;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2024/1/10 10:46
 */
@Data
public class GameQuestionSubmitRequest
{
    /**
     * 竞赛id
     */
    private Long gameId;

    /**
     * 题目提交信息
     */
    private QuestionSubmitAddRequest questionSubmitAddRequest;

    private static final long serialVersionUID = 1L;
}
