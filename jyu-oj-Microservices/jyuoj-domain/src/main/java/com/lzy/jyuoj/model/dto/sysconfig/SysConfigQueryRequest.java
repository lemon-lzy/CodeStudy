package sspu.zzx.sspuoj.model.dto.sysconfig;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sspu.zzx.sspuoj.common.PageRequest;

import java.io.Serializable;
import java.util.Date;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/11/24 16:05
 * @Description 系统配置查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysConfigQueryRequest extends PageRequest implements Serializable
{
    /**
     * id
     */
    private Long id;

    /**
     * 参数名称
     */
    private String configName;

    /**
     * 参数键名
     */
    private String configKey;


    /**
     * 参数类型
     */
    private String configType;


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
