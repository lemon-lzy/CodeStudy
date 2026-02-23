package sspu.zzx.sspuoj.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新请求
 *
 * @author ZZX
 * @from SSPU
 */
@Data
public class QuestionUpdateRequest implements Serializable
{

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 题目答案
     */
    private String answer;

    /**
     * 答案模板
     * language：value
     * 模板语言：模板内容
     */
    private String answerTemplate;

    /**
     * 判题用例
     */
    private List<JudgeCase> judgeCase;

    /**
     * 判题配置
     */
    private JudgeConfig judgeConfig;

    // todo 添加题目类型枚举类
    /**
     * 题目类型：代码题和文字题
     */
    private String questionType;

    // todo 合理处理题目的私有化逻辑
    /**
     * 是否私有化：即不公开，只允许自己或所在组织查看
     */
    private Boolean isPrivate;

    /**
     * 题目难度：简单、中等、困难
     */
    private String questionDifficulty;

    private static final long serialVersionUID = 1L;
}