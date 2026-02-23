package sspu.zzx.sspuoj.model.dto.feedback;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sspu.zzx.sspuoj.common.PageRequest;

import java.io.Serializable;

/**
 * 反馈查询请求
 *
 * @author ZZX
 * @from SSPU
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FeedbackQueryRequest extends PageRequest implements Serializable
{
    /**
     * id
     */
    private Long id;

    /**
     * 申请人名称
     */
    private String applierName;

    /**
     * 处理人名称
     */
    private String handlerName;

    /**
     * 申请内容
     */
    private String applierContext;

    /**
     * 申请类型
     */
    private String type;

    /**
     * 处理状态
     */
    private String handleState;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    private static final long serialVersionUID = 1L;
}