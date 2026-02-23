package sspu.zzx.sspuoj.model.vo.game;

import lombok.Data;
import sspu.zzx.sspuoj.model.vo.question.QuestionVO;

import java.io.Serializable;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2024/1/10 13:24
 */
@Data
public class GameQuestionVo implements Serializable
{
    /**
     * 题目信息
     */
    private QuestionVO questionVO;

    /**
     * 题目总分
     */
    private Integer fullScore;

    private static final long serialVersionUID = 1L;
}
