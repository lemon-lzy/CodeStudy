package sspu.zzx.sspuoj.model.dto.questionlist;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ZZX
 */
@Data
public class QuestionListAddRequest implements Serializable
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
     * 题单简介
     */
    private QuestionListProfile listProfile;

    /**
     * 题单头像（图像链接）
     */
    private String listAvatar;

    /**
     * 题单可见区域
     */
    private String publicZone;

    /**
     * 创建者id（空则后端获取）
     */
    private Long creatorId;

    /**
     * 创作者姓名（空则后端获取）
     */
    private String creatorName;

    /**
     * 题目包含的题目id集合
     */
    private List<Long> questionIdList;

    private static final long serialVersionUID = 1L;
}