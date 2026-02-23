package sspu.zzx.sspuoj.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.judge.codesandbox.CodeSandbox;
import sspu.zzx.sspuoj.model.judge.model.ExecuteCodeRequest;
import sspu.zzx.sspuoj.model.judge.model.ExecuteCodeResponse;

/**
 * 远程代码沙箱（实际调用接口的沙箱）
 *
 * @author ZZX
 */
@Service
@Slf4j
public class RemoteCodeSandbox implements CodeSandbox
{

    /**
     * todo api地址，默认本地项目，可以换成服务器的
     */
    public static final String CODE_SANDBOX_API_URL = "http://localhost:8090/codesandbox/run";

    /**
     * 鉴权请求头
     */
    public static final String AUTH_REQUEST_HEADER = "sspuoj-codesandbox-auth-by-zzx";


    /**
     * 鉴权请求密钥
     */
    public static final String AUTH_REQUEST_SECRET = "$W$~vrZwe7z&L!ht^U%fF2zZzHTjWSwY%@ZeEJ^*(qZ()D3npx";


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest)
    {
        log.info("调用远程代码沙箱，url：{}，请求参数：{}", CODE_SANDBOX_API_URL, executeCodeRequest);
        // 调用远程接口
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(CODE_SANDBOX_API_URL).header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET).body(json).execute().body();
        log.info("调用远程代码沙箱，url：{}，返回结果：{}", CODE_SANDBOX_API_URL, responseStr);
        if (StringUtils.isBlank(responseStr))
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "executeCode remoteSandbox error, message = " + responseStr);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}
