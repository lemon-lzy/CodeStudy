package sspu.zzx.sspuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import sspu.zzx.sspuoj.common.PageRequest;
import sspu.zzx.sspuoj.model.dto.operationlog.OperationLogQueryRequest;
import sspu.zzx.sspuoj.model.dto.user.UserQueryRequest;
import sspu.zzx.sspuoj.model.entity.OperationLog;

import java.util.List;


/**
 * @author ZZX
 * @description 针对表【operation_log】的数据库操作Service
 * @createDate 2023-11-15 14:36:26
 */
public interface OperationLogService extends IService<OperationLog>
{
    List<OperationLog> getOnlineUserList(List<Object> loginList);

    QueryWrapper<OperationLog> getQueryWrapper(OperationLogQueryRequest operationLogQueryRequest);
}
