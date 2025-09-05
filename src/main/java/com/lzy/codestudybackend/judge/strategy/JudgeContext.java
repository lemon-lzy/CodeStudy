package com.lzy.codestudybackend.judge.strategy;

import com.lzy.codestudybackend.judge.codasandbox.model.JudgeInfo;
import com.lzy.codestudybackend.model.dto.questionCode.JudgeCase;
import com.lzy.codestudybackend.model.entity.question.QuestionCode;
import com.lzy.codestudybackend.model.entity.question.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 */
@Data
public class JudgeContext {

    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private QuestionCode questionCode;

    private QuestionSubmit questionSubmit;

}
