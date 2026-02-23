package sspu.zzx.sspuoj.model.dto.operationlog;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sspu.zzx.sspuoj.common.PageRequest;

import java.io.Serializable;
import java.util.Date;

/**
 * 操作日志查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OperationLogQueryRequest extends PageRequest implements Serializable
{
    /**
     * id
     */
    private Long id;

    /**
     * 表名（注意存到枚举）
     */
    private String tName;

    /**
     * 操作者ip
     */
    private String operatorIp;

    /**
     * 操作者客户端
     */
    private String operatorClient;

    /**
     * 操作名称
     */
    private String operationName;

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