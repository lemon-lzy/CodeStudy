package com.lzy.codestudybackend.judge.codasandbox.impl.python3;

import com.lzy.codestudybackend.judge.codasandbox.model.ExecuteCodeRequest;
import com.lzy.codestudybackend.judge.codasandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;


/**
 * Python 原生代码沙箱实现（直接复用模板方法）
 *
 * @author 15712
 */
@Component
public class PythonNativeCodeSandbox extends PythonCodeSandBoxTemplate {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}

