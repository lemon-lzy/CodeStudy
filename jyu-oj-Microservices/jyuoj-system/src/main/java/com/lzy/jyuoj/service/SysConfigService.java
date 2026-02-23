package sspu.zzx.sspuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import sspu.zzx.sspuoj.model.dto.sysconfig.SysConfigQueryRequest;
import sspu.zzx.sspuoj.model.entity.SysConfig;


/**
 * @author ZZX
 * @description 针对表【sys_config】的数据库操作Service
 * @createDate 2023-11-15 14:37:06
 */
public interface SysConfigService extends IService<SysConfig>
{

    QueryWrapper<SysConfig> getQueryWrapper(SysConfigQueryRequest sysConfigQueryRequest);
}
