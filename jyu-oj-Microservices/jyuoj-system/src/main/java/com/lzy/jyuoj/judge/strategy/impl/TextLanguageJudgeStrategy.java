package sspu.zzx.sspuoj.judge.strategy.impl;

import cn.hutool.json.JSONUtil;
import sspu.zzx.sspuoj.judge.strategy.JudgeContext;
import sspu.zzx.sspuoj.judge.strategy.JudgeStrategy;
import sspu.zzx.sspuoj.model.dto.question.JudgeCase;
import sspu.zzx.sspuoj.model.dto.question.JudgeConfig;
import sspu.zzx.sspuoj.model.entity.Question;
import sspu.zzx.sspuoj.model.entity.QuestionSubmit;
import sspu.zzx.sspuoj.model.enums.JudgeInfoMessageEnum;
import sspu.zzx.sspuoj.model.judge.model.JudgeInfo;

import java.util.List;
import java.util.Optional;

/**
 * 文字题的判题策略
 * 对应的语言 plain text
 */
public class TextLanguageJudgeStrategy implements JudgeStrategy
{

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext)
    {

        // 记录初始内存使用情况
        long initialMemory = getUsedMemory();

        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 判题
        Question question = judgeContext.getQuestion();
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;
        if (!question.getAnswer().equals(questionSubmit.getCode()))
        {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
        }
        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());

        // 记录结束时间
        long endTime = System.currentTimeMillis();

        // 计算执行时间
        long executionTime = endTime - startTime;
        judgeInfoResponse.setTime(executionTime);

        // 记录执行后的内存使用情况
        long finalMemory = getUsedMemory();
        // 计算内存使用量，单位字节，转换成kb需要除以1024
        long memoryUsage = finalMemory - initialMemory;
        judgeInfoResponse.setMemory(memoryUsage / 1024);

        return judgeInfoResponse;
    }

    // 获取当前已使用的内存量
    private long getUsedMemory()
    {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
