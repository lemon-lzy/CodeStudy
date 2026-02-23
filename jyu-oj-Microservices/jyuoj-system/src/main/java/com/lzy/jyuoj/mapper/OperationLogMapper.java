package sspu.zzx.sspuoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sspu.zzx.sspuoj.model.entity.OperationLog;

import java.util.List;


/**
 * @author ZZX
 * @description 针对表【operation_log】的数据库操作Mapper
 * @createDate 2023-11-15 14:36:26
 * @Entity sspu.zzx.sspuoj.model.OperationLog
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog>
{

    List<OperationLog> getOnlineUserList(@Param("loginList") List<String> loginList);

}




