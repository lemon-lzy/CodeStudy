package com.lzy.codestudybackend.judge.codasandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.lzy.codestudybackend.judge.codasandbox.model.ExecuteCodeResponse;
import com.lzy.codestudybackend.judge.codasandbox.model.ExecuteMessage;
import com.lzy.codestudybackend.judge.codasandbox.model.JudgeInfo;
import com.lzy.codestudybackend.model.enums.QuestionSubmitStatusEnum;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 代码沙箱一些通用方法
 *
 * @version 1.0
 * @Author ZZX
 * @Date 2024/1/19 14:09
 */
public class CommonCodeSandboxTemplate
{
    /**
     * 把用户的代码保存为文件
     *
     * @param code           用户代码
     * @param globalCodePath 全局代码路径
     * @param fileName       文件名
     * @return
     */
    public File saveCodeToFile(String code, String globalCodePath, String fileName)
    {
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + globalCodePath;
        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName))
        {
            FileUtil.mkdir(globalCodePathName);
        }

        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + fileName;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        return userCodeFile;
    }


    /**
     * 获取输出结果
     *
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList)
    {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取用时最大值，便于判断是否超时
        long maxTime = 0;
        long maxMemory = 0;
        for (ExecuteMessage executeMessage : executeMessageList)
        {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage))
            {
                executeCodeResponse.setMessage(executeMessage.getMessage());
                // 用户提交的代码执行中存在错误
                executeCodeResponse.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
                JudgeInfo judgeInfo=new JudgeInfo();
                judgeInfo.setMessage(errorMessage);
                executeCodeResponse.setJudgeInfo(judgeInfo);
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if (time != null)
            {
                maxTime = Math.max(maxTime, time);
            }
            Long memory = executeMessage.getMemory();
            if (memory != null)
            {
                maxMemory = Math.max(maxMemory, memory);
            }
        }
        // 正常运行完成
        if (outputList.size() == executeMessageList.size())
        {
            executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
            executeCodeResponse.setMessage(QuestionSubmitStatusEnum.SUCCEED.getText());
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(maxMemory);
        // 运行正常完成则不设置message，交由判题机判题
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 删除文件
     *
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile)
    {
        if (userCodeFile.getParentFile() != null)
        {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }

}
