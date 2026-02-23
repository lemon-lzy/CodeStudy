package sspu.zzx.sspuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.constant.CommonConstant;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.mapper.SysConfigMapper;
import sspu.zzx.sspuoj.model.dto.sysconfig.SysConfigQueryRequest;
import sspu.zzx.sspuoj.model.entity.OperationLog;
import sspu.zzx.sspuoj.model.entity.SysConfig;
import sspu.zzx.sspuoj.service.SysConfigService;
import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.utils.SqlUtils;

import java.util.Date;

/**
 * @author ZZX
 * @description 针对表【sys_config】的数据库操作Service实现
 * @createDate 2023-11-15 14:37:06
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService
{

    @Override
    public QueryWrapper<SysConfig> getQueryWrapper(SysConfigQueryRequest sysConfigQueryRequest)
    {
        if (sysConfigQueryRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = sysConfigQueryRequest.getId();
        String configName = sysConfigQueryRequest.getConfigName();
        String configKey = sysConfigQueryRequest.getConfigKey();
        String configType = sysConfigQueryRequest.getConfigType();
        String startTime = sysConfigQueryRequest.getStartTime();
        String endTime = sysConfigQueryRequest.getEndTime();
        String sortField = sysConfigQueryRequest.getSortField();
        String sortOrder = sysConfigQueryRequest.getSortOrder();

        QueryWrapper<SysConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(configName), "configName", configName);
        queryWrapper.like(StringUtils.isNotBlank(configKey), "configKey", configKey);
        queryWrapper.like(StringUtils.isNotBlank(configType), "configType", configType);
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime))
        {
            queryWrapper.between("createTime", startTime, endTime);
        }
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);

        return queryWrapper;
    }
}




