package sspu.zzx.sspuoj.model.dto.sysconfig;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sspu.zzx.sspuoj.common.PageRequest;

import java.io.Serializable;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/11/24 16:31
 * @Description 系统配置更新请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysConfigUpdateRequest extends PageRequest implements Serializable
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
     * 参数值
     */
    private String configValue;

    /**
     * 备注
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}
