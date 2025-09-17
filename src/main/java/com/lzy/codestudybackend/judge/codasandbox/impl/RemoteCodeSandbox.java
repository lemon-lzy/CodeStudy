package com.lzy.codestudybackend.judge.codasandbox.impl;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.StringUtils;
import com.lzy.codestudybackend.constant.AuthConstant;
import com.lzy.codestudybackend.exception.BusinessException;
import com.lzy.codestudybackend.judge.codasandbox.CodeSandbox;
import com.lzy.codestudybackend.judge.codasandbox.model.ExecuteCodeRequest;
import com.lzy.codestudybackend.judge.codasandbox.model.ExecuteCodeResponse;
import com.lzy.codestudybackend.common.ErrorCode;

/**
 * 远程代码沙箱（实际调用接口的沙箱）
 */
public class RemoteCodeSandbox implements CodeSandbox {




    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱");
        String url = "http://192.168.150.105:8123/executeCode";
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(url)
                .header(AuthConstant.AUTHREQUESTHEADER, AuthConstant.AUTHREQUESTSECRET)
                .body(json)
                .execute()
                .body();
        if (StringUtils.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "executeCode remoteSandbox error, message = " + responseStr);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}

