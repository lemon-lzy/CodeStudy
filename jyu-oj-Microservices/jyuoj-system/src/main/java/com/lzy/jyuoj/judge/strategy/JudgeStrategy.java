package sspu.zzx.sspuoj.judge.strategy;

import sspu.zzx.sspuoj.model.judge.model.JudgeInfo;

/**
 * 判题策略
 * @author ZZX
 */
public interface JudgeStrategy {

    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
