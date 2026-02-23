package sspu.zzx.sspuoj.judge;

import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.judge.strategy.*;
import sspu.zzx.sspuoj.judge.strategy.impl.DefaultJudgeStrategy;
import sspu.zzx.sspuoj.judge.strategy.impl.JavaLanguageJudgeStrategy;
import sspu.zzx.sspuoj.judge.strategy.impl.Python3LanguageJudgeStrategy;
import sspu.zzx.sspuoj.judge.strategy.impl.TextLanguageJudgeStrategy;
import sspu.zzx.sspuoj.model.entity.Question;
import sspu.zzx.sspuoj.model.entity.QuestionSubmit;
import sspu.zzx.sspuoj.model.enums.QuestionSubmitLanguageEnum;
import sspu.zzx.sspuoj.model.enums.TypeEnum;
import sspu.zzx.sspuoj.model.judge.model.JudgeInfo;

/**
 * 判题管理（简化调用）
 * @author ZZX
 */
@Service
public class JudgeManager
{

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext)
    {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        Question question = judgeContext.getQuestion();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if (TypeEnum.ROUTINE_QUESTION.getValue().equals(question.getQuestionType()) && QuestionSubmitLanguageEnum.PLAINTEXT.getValue().equals(language))
        {
            judgeStrategy = new TextLanguageJudgeStrategy();
        }
        else if (QuestionSubmitLanguageEnum.JAVA.getValue().equals(language))
        {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        else if (QuestionSubmitLanguageEnum.PYTHON3.getValue().equals(language))
        {
            judgeStrategy = new Python3LanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}
