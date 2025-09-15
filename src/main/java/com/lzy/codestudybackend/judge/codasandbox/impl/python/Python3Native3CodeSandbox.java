package com.lzy.codestudybackend.judge.codasandbox.impl.python;

import com.lzy.codestudybackend.judge.codasandbox.model.ExecuteCodeRequest;
import com.lzy.codestudybackend.judge.codasandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;
/**
 * Python3 原生代码沙箱实现（直接复用模板方法）
 *
 * @author zzx
 */
@Component
public class Python3Native3CodeSandbox extends Python3CodeSandboxTemplate
{

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest)
    {
        return super.executeCode(executeCodeRequest);
    }
}
