package sspu.zzx.sspuoj.judge;

import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.judge.codesandbox.CodeSandbox;
import sspu.zzx.sspuoj.judge.codesandbox.CodeSandboxFactory;
import sspu.zzx.sspuoj.judge.codesandbox.CodeSandboxProxy;
import sspu.zzx.sspuoj.judge.strategy.JudgeContext;
import sspu.zzx.sspuoj.model.dto.question.JudgeCase;
import sspu.zzx.sspuoj.model.entity.Question;
import sspu.zzx.sspuoj.model.entity.QuestionSubmit;
import sspu.zzx.sspuoj.model.enums.JudgeInfoMessageEnum;
import sspu.zzx.sspuoj.model.enums.QuestionSubmitStatusEnum;
import sspu.zzx.sspuoj.model.enums.TypeEnum;
import sspu.zzx.sspuoj.model.judge.model.ExecuteCodeRequest;
import sspu.zzx.sspuoj.model.judge.model.ExecuteCodeResponse;
import sspu.zzx.sspuoj.model.judge.model.JudgeInfo;
import sspu.zzx.sspuoj.service.QuestionService;
import sspu.zzx.sspuoj.service.QuestionSubmitService;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService
{

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeManager judgeManager;

    @Value("${codesandbox.type}")
    private String type;


    @Override
    public QuestionSubmit doJudge(long questionSubmitId)
    {
        // 1）传入题目的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null)
        {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null)
        {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2）如果题目提交状态不为等待中，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue()))
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3）更改判题（题目提交）的状态为 “判题中”，防止重复执行
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update)
        {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        JudgeContext judgeContext = new JudgeContext();
        boolean needJudge = true;
        // 判断是否是编程题
        if (TypeEnum.CODE_QUESTION.getValue().equals(question.getQuestionType()))
        {
            // 4）调用沙箱，获取到执行结果
            CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
            codeSandbox = new CodeSandboxProxy(codeSandbox);
            String language = questionSubmit.getLanguage();
            String code = questionSubmit.getCode();
            // 获取输入用例
            String judgeCaseStr = question.getJudgeCase();
            List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
            List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
            ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder().code(code).language(language).inputList(inputList).build();
            ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
            // 5）根据沙箱的执行结果，设置题目的判题状态和信息
            List<String> outputList = executeCodeResponse.getOutputList();
            String message = executeCodeResponse.getMessage();
            Integer status = executeCodeResponse.getStatus();
            JudgeInfo judgeInfo = executeCodeResponse.getJudgeInfo();
            questionSubmitUpdate.setStatus(status);
            if (!QuestionSubmitStatusEnum.SUCCEED.getValue().equals(status))
            {
                judgeInfo.setMessage(message);
                judgeInfo.setMemory(0L);
                judgeInfo.setTime(0L);
                questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
                needJudge = false;
            }
            judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
            judgeContext.setInputList(inputList);
            judgeContext.setOutputList(outputList);
            judgeContext.setJudgeCaseList(judgeCaseList);
            judgeContext.setQuestion(question);
            judgeContext.setQuestionSubmit(questionSubmit);
        }
        // 否则为常规题
        else
        {
            judgeContext.setQuestion(question);
            judgeContext.setQuestionSubmit(questionSubmit);
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        }
        // 判题
        if (needJudge)
        {
            JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
            questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        }
        // 6）修改数据库中的判题结果
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update)
        {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionSubmitId);
        return questionSubmitResult;
    }
}
