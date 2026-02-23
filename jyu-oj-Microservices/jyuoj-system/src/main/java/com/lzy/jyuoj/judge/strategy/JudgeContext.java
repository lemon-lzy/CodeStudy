package sspu.zzx.sspuoj.judge.strategy;

import lombok.Data;
import sspu.zzx.sspuoj.model.dto.question.JudgeCase;
import sspu.zzx.sspuoj.model.entity.Question;
import sspu.zzx.sspuoj.model.entity.QuestionSubmit;
import sspu.zzx.sspuoj.model.judge.model.JudgeInfo;

import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 * @author ZZX
 */
@Data
public class JudgeContext {

    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private Question question;

    private QuestionSubmit questionSubmit;

}
