package com.lzy.codestudybackend.judge;
import com.lzy.codestudybackend.model.dto.questionSubmit.QuestionSubmitAddRequest;
import com.lzy.codestudybackend.model.entity.question.QuestionSubmit;

/**
 * 判题服务 ：执行代码
 */
public interface JudgeService {
    /**
     * 判题
     *
     * @param questionSubmitId
     * @return
     */
    // QuestionSubmit doJudge(long questionSubmitId);

    /**
     * 判题
     *
     * @param questionSubmitId
     * @return
     */
    QuestionSubmit doJudge(long questionSubmitId, QuestionSubmitAddRequest questionSubmitAddRequest);
}
