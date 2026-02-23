package sspu.zzx.sspuoj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName operation_log
 */
@TableName(value = "operation_log")
@Data
public class OperationLog implements Serializable
{
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 表名（注意存到枚举）
     */
    private String tName;

    /**
     * 操作者id（注意处理null）
     */
    private Long operatorId;

    /**
     * 操作者ip
     */
    private String operatorIp;

    /**
     * 操作者浏览器
     */
    private String operatorBrowser;

    /**
     * 操作者客户端
     */
    private String operatorClient;

    /**
     * 操作者系统
     */
    private String operatorOs;

    /**
     * 操作者token
     */
    private String operatorToken;

    /**
     * 操作名称
     */
    private String operationName;

    /**
     * 操作方法
     */
    private String operationMethod;

    /**
     * 操作参数
     */
    private String operationParams;

    /**
     * 操作结果
     */
    private String operationResult;

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