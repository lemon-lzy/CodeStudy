package sspu.zzx.sspuoj.model.dto.questionsolution;

import lombok.Data;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author ZZX
 * @from SSPU
 */
@Data
public class QuestionSolutionAddRequest implements Serializable
{
    /**
     * 题目id
     */
    private Long questionId;


    /**
     * 题解标题
     */
    private String title;

    /**
     * 题解简介/外站链接
     */
    private String introduction;

    /**
     * 题解内容
     */
    private String solution;

    /**
     * 题解类型
     */
    private String type;

    /**
     * 外站作者名（充当）
     */
    private String authorName;

    /**
     * 外站封面图片链接（充当）
     */
    private String authorAvatar;


    private static final long serialVersionUID = 1L;
}