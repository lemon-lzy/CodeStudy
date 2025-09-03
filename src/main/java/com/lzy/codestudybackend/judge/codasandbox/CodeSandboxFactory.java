package com.lzy.codestudybackend.judge.codasandbox;


import com.lzy.codestudybackend.judge.codasandbox.impl.AICodeSandbox;
import com.lzy.codestudybackend.judge.codasandbox.impl.ExampleCodeSandbox;
import com.lzy.codestudybackend.judge.codasandbox.impl.RemoteCodeSandbox;
import com.lzy.codestudybackend.judge.codasandbox.impl.ThirdPartyCodeSandbox;
import com.lzy.codestudybackend.utils.SpringContextUtils;
import org.springframework.stereotype.Component;

/**
 * 代码沙箱工厂（根据字符串参数创建指定的代码沙箱实例），
 */
@Component
public class CodeSandboxFactory {

    /**
     * 创建代码沙箱实例
     *
     * @param type 沙箱类型
     * @return
     */
    public static CodeSandbox newInstance(String type) {
        switch (type) {
            case "example":
                return new ExampleCodeSandbox();
            case "remote":
                return new RemoteCodeSandbox();
            case "thirdParty":
                return new ThirdPartyCodeSandbox();
            case "ai":
                return SpringContextUtils.getBean(AICodeSandbox.class);
            default:
                return new ExampleCodeSandbox();
        }
    }
}
