package com.lzy.codestudybackend.judge.codasandbox.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判题信息
 */
@Data
@NoArgsConstructor
public class JudgeInfo {

    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 消耗内存
     */
    private Long memory;

    /**
     * 消耗时间（KB）
     */
    private Long time;


    public JudgeInfo(String message,Long memory,Long time)
    {
        this.message=message;
        this.memory=memory;
        this.time=time;
    }
}
