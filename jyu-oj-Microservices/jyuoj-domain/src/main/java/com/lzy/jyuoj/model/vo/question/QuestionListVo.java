package sspu.zzx.sspuoj.model.vo.question;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/12/25 20:00
 */
@Data
public class QuestionListVo
{

    /**
     * 题单id
     */
    private Long id;

    /**
     * 题单名称
     */
    private String listName;

    /**
     * 题单作者id
     */
    private Long creatorId;

    /**
     * 创作者姓名
     */
    private String creatorName;

    /**
     * 公开范围
     */
    private String publicZone;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 题目数量，动态生成
     */
    private Long questionCount;

    private static final long serialVersionUID = 1L;
}