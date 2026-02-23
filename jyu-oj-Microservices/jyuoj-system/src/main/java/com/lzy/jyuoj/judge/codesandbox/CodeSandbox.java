package sspu.zzx.sspuoj.judge.codesandbox;

import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.model.judge.model.ExecuteCodeRequest;
import sspu.zzx.sspuoj.model.judge.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口定义
 * @author ZZX
 */
@Service
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
