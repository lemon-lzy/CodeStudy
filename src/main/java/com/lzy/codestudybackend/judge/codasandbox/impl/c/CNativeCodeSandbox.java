package com.lzy.codestudybackend.judge.codasandbox.impl.c;
import com.lzy.codestudybackend.judge.codasandbox.model.ExecuteCodeRequest;
import com.lzy.codestudybackend.judge.codasandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;


/**
 * C 原生代码沙箱实现（直接复用模板方法）
 *
 * @author 15712
 */
@Component
public class CNativeCodeSandbox extends CCodeSandboxTemplate {

    /**
     * 执行代码
     *
     * @return
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}

