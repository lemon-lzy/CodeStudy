package com.lzy.codestudybackend.judge;

import com.lzy.codestudybackend.judge.codasandbox.model.JudgeInfo;
import com.lzy.codestudybackend.judge.strategy.DefaultJudgeStrategy;
import com.lzy.codestudybackend.judge.strategy.JavaLanguageJudgeStrategy;
import com.lzy.codestudybackend.judge.strategy.JudgeContext;
import com.lzy.codestudybackend.judge.strategy.JudgeStrategy;
import com.lzy.codestudybackend.model.entity.question.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getSubmitLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}