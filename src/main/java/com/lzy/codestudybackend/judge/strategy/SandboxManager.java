package com.lzy.codestudybackend.judge.strategy;

import com.lzy.codestudybackend.judge.codasandbox.CodeSandbox;
import com.lzy.codestudybackend.judge.codasandbox.impl.c.CNativeCodeSandbox;
import com.lzy.codestudybackend.judge.codasandbox.impl.cpp.CppNativeCodeSandbox;
import com.lzy.codestudybackend.judge.codasandbox.impl.java.JavaNativeCodeSandbox;
import com.lzy.codestudybackend.judge.codasandbox.impl.python3.PythonNativeCodeSandbox;
import org.springframework.stereotype.Service;

/**
 * @author 15712
 * 判题管理，简化判题服务
 */
@Service
public class SandboxManager {


    public static CodeSandbox getSandBox(String language) {
        switch (language) {
            case "java":
                return new JavaNativeCodeSandbox();
            case "python":
                return new PythonNativeCodeSandbox();
            case "c":
                return new CNativeCodeSandbox();
            case "cpp":
                return new CppNativeCodeSandbox();
            default:
                throw new RuntimeException("不支持该语言");
        }

    }
}
