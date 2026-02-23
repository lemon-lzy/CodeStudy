package sspu.zzx.sspuoj.model.dto.questionsolution;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sspu.zzx.sspuoj.common.PageRequest;

import java.io.Serializable;
import java.util.List;

/**
 * 查询请求
 *
 * @author ZZX
 * @from SSPU
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionSolutionQueryRequest extends PageRequest implements Serializable
{

    /**
     * id
     */
    private Long id;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 作者id
     */
    private Long userId;

    /**
     * 作者名称
     */
    private String authorName;

    /**
     * 题解标题
     */
    private String title;

    /**
     * 题解类型
     */
    private String type;


    private static final long serialVersionUID = 1L;
}