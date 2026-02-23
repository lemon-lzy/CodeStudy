package sspu.zzx.sspuoj.model.dto.organization;

import lombok.Data;
import sspu.zzx.sspuoj.common.PageRequest;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/12/28 13:31
 */
@Data
public class OrganizationQueryRequest extends PageRequest
{
    /**
     * id
     */
    private Long id;

    /**
     * 组织名称
     */
    private String organName;

    /**
     * 组织当前人数
     */
    private Integer organCurrentNum;

    /**
     * 组织最大人数
     */
    private Integer organTotalNum;

    /**
     * 组织创始人Id
     */
    private Long organizerId;

    /**
     * 组织创始人名称
     */
    private String organizerName;

    /**
     * 创建时间
     */
    private String createTime;


    private static final long serialVersionUID = 1L;
}
