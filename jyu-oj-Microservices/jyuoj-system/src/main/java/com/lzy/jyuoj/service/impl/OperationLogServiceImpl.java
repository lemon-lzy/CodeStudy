package sspu.zzx.sspuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.constant.CommonConstant;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.mapper.OperationLogMapper;
import sspu.zzx.sspuoj.model.dto.operationlog.OperationLogQueryRequest;
import sspu.zzx.sspuoj.model.entity.OperationLog;
import sspu.zzx.sspuoj.service.OperationLogService;
import sspu.zzx.sspuoj.utils.SqlUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ZZX
 * @description 针对表【operation_log】的数据库操作Service实现
 * @createDate 2023-11-15 14:36:26
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService
{

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public List<OperationLog> getOnlineUserList(List<Object> loginList)
    {
        if (loginList != null && loginList.size() > 0)
        {
            List<String> list = loginList.stream().map(item -> (String) item).collect(Collectors.toList());

            return operationLogMapper.getOnlineUserList(list);
        }
        return new ArrayList<>();
    }

    @Override
    public QueryWrapper<OperationLog> getQueryWrapper(OperationLogQueryRequest operationLogQueryRequest)
    {
        if (operationLogQueryRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = operationLogQueryRequest.getId();
        String tName = operationLogQueryRequest.getTName();
        String operatorIp = operationLogQueryRequest.getOperatorIp();
        String operatorClient = operationLogQueryRequest.getOperatorClient();
        String operationName = operationLogQueryRequest.getOperationName();
        String sortField = operationLogQueryRequest.getSortField();
        String sortOrder = operationLogQueryRequest.getSortOrder();
        String startTime = operationLogQueryRequest.getStartTime();
        String endTime = operationLogQueryRequest.getEndTime();

        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(tName), "tName", tName);
        queryWrapper.like(StringUtils.isNotBlank(operationName), "operationName", operationName);
        queryWrapper.like(StringUtils.isNotBlank(operatorClient), "operatorClient", operatorClient);
        queryWrapper.like(StringUtils.isNotBlank(operatorIp), "operatorIp", operatorIp);
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime))
        {
            queryWrapper.between("createTime", startTime, endTime);
        }
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);

        return queryWrapper;
    }

}




