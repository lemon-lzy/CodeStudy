package com.lzy.codestudybackend.judge.strategy;

import com.lzy.codestudybackend.judge.codasandbox.model.JudgeInfo;

/**
 * 判题策略
 */
public interface JudgeStrategy {

    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}