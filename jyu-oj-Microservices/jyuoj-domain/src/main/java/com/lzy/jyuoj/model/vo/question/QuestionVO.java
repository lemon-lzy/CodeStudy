package sspu.zzx.sspuoj.model.vo.question;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import sspu.zzx.sspuoj.model.dto.question.JudgeConfig;
import sspu.zzx.sspuoj.model.entity.Question;
import sspu.zzx.sspuoj.model.vo.user.UserVO;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目封装类
 *
 * @TableName question
 */
@Data
public class QuestionVO implements Serializable
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
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 题目难度：简单、中等、困难
     */
    private String questionDifficulty;

    /**
     * 题目类型
     */
    private String questionType;

    /**
     * 题目答案模板
     * json-Map
     * 格式：{"language":"${template_value}"}
     * 编程题作为代码模板
     * 非编程题作为文本模板，起到提示的作用
     * 因map无法直接转，先搞成list，元素是[key,val]
     */
    private String answerTemplate;

    /**
     * 是否公开（默认公开）
     */
    private Boolean isPrivate;

    /**
     * 判题配置（json 对象）
     */
    private JudgeConfig judgeConfig;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建题目人的信息
     */
    private UserVO userVO;

    /**
     * 包装类转对象
     *
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO)
    {
        if (questionVO == null)
        {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionVO, question);
        List<String> tagList = questionVO.getTags();
        if (tagList != null)
        {
            question.setTags(JSONUtil.toJsonStr(tagList));
        }
        JudgeConfig voJudgeConfig = questionVO.getJudgeConfig();
        if (voJudgeConfig != null)
        {
            question.setJudgeConfig(JSONUtil.toJsonStr(voJudgeConfig));
        }
        return question;
    }

    /**
     * 对象转包装类
     *
     * @param question
     * @return
     */
    public static QuestionVO objToVo(Question question)
    {
        if (question == null)
        {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        List<String> tagList = JSONUtil.toList(question.getTags(), String.class);
        questionVO.setTags(tagList);
        String judgeConfigStr = question.getJudgeConfig();
        questionVO.setJudgeConfig(JSONUtil.toBean(judgeConfigStr, JudgeConfig.class));
        return questionVO;
    }

    private static final long serialVersionUID = 1L;
}