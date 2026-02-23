package sspu.zzx.sspuoj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ZZX
 * @TableName feedback
 */
@TableName(value = "feedback")
@Data
public class Feedback implements Serializable
{
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 反馈人id
     */
    private Long applierId;

    /**
     * 申请人名称
     */
    private String applierName;

    /**
     * 反馈内容
     */
    private String applierContext;

    /**
     * 处理人id
     */
    private Long handlerId;

    /**
     * 处理人名称
     */
    private String handlerName;

    /**
     * 反馈类型
     */
    private String type;

    /**
     * 处理状态（0-未；1-成）
     */
    private String handleState;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}