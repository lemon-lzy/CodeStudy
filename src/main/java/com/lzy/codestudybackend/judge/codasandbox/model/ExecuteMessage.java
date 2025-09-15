package com.lzy.codestudybackend.judge.codasandbox.model;

import lombok.Data;

/**
 * 进程执行信息
 *
 * @author zzx
 */
@Data
public class ExecuteMessage
{

    /**
     * 执行结果
     * 0-正常
     * 1（非零）-异常
     */
    private Integer exitValue;

    private String message;

    private String errorMessage;

    private Long time;

    private Long memory;
}
