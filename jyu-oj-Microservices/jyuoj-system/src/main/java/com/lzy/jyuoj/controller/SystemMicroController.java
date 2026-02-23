package sspu.zzx.sspuoj.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import sspu.zzx.sspuoj.aop.annotation.OpLog;
import sspu.zzx.sspuoj.common.BaseResponse;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.common.ResultUtils;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.exception.ThrowUtils;
import sspu.zzx.sspuoj.model.dto.operationlog.OperationLogQueryRequest;
import sspu.zzx.sspuoj.model.dto.server.Server;
import sspu.zzx.sspuoj.model.dto.sysconfig.SysConfigQueryRequest;
import sspu.zzx.sspuoj.model.dto.sysconfig.SysConfigUpdateRequest;
import sspu.zzx.sspuoj.model.entity.OperationLog;
import sspu.zzx.sspuoj.model.entity.SysConfig;
import sspu.zzx.sspuoj.service.OperationLogService;
import sspu.zzx.sspuoj.service.SysConfigService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static sspu.zzx.sspuoj.constant.UserConstant.ADMIN_ROLE;

/**
 * 系统接口
 *
 * @author ZZX
 */
@RestController
@RequestMapping("/system")
public class SystemMicroController
{
    @Resource
    private OperationLogService operationLogService;

    @Resource
    private SysConfigService sysConfigService;

    /*服务器信息：ServerController*/

    /**
     * 获取服务器信息
     *
     * @return
     * @throws Exception
     */
    @SaCheckRole(ADMIN_ROLE)
    @GetMapping("/monitor/serverInfo")
    public BaseResponse<Server> getInfo() throws Exception
    {
        Server server = new Server();
        server.copyTo();
        return ResultUtils.success(server);
    }

    /*操作日志：OperationLogController*/

    /**
     * 分页检索操作日志
     *
     * @param operationLogQueryRequest
     * @return
     */
    @PostMapping("/config/operationLog/query")
    public BaseResponse<Page<OperationLog>> getOperationLogList(@RequestBody OperationLogQueryRequest operationLogQueryRequest)
    {
        long current = operationLogQueryRequest.getCurrent();
        long size = operationLogQueryRequest.getPageSize();
        Page<OperationLog> operationLogPage = operationLogService.page(new Page<>(current, size), operationLogService.getQueryWrapper(operationLogQueryRequest));
        return ResultUtils.success(operationLogPage);
    }

    /**
     * 根据id删除对应日志
     *
     * @param operationLog
     * @return
     */
    @DeleteMapping("/config/operationLog/delete")
    public BaseResponse<Boolean> deleteOperationLogById(@RequestBody OperationLog operationLog)
    {
        if (operationLog == null || operationLog.getId() <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean remove = operationLogService.removeById(operationLog.getId());
        ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /*参数配置：SysConfigController*/

    /**
     * 分页检索用户对应的反馈
     *
     * @param sysConfigQueryRequest
     * @return
     */
    @SaCheckRole(ADMIN_ROLE)
    @PostMapping("/config/query")
    public BaseResponse<Page<SysConfig>> getSysConfigList(@RequestBody SysConfigQueryRequest sysConfigQueryRequest)
    {
        long current = sysConfigQueryRequest.getCurrent();
        long size = sysConfigQueryRequest.getPageSize();
        Page<SysConfig> sysConfigPage = sysConfigService.page(new Page<>(current, size), sysConfigService.getQueryWrapper(sysConfigQueryRequest));
        return ResultUtils.success(sysConfigPage);
    }

    /**
     * 根据key获取value
     *
     * @param configKey
     * @return
     */
    @GetMapping("/config/getByConfigKey")
    public BaseResponse<SysConfig> getConfigByKey(@RequestParam String configKey)
    {
        if (StringUtils.isBlank(configKey))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        QueryWrapper<SysConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("configKey", configKey);
        SysConfig sysConfig = sysConfigService.getOne(wrapper);
        if (sysConfig == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不存在");
        }
        return ResultUtils.success(sysConfig);
    }

    /**
     * 新增系统配置字典记录
     *
     * @param sysConfig
     * @return
     */
    @SaCheckRole(ADMIN_ROLE)
    @OpLog("新增参数配置:sys_config")
    @PostMapping("/config/add")
    public BaseResponse<Boolean> addSysConfig(@RequestBody SysConfig sysConfig)
    {
        if (sysConfig == null || StringUtils.isAnyBlank(sysConfig.getConfigKey(), sysConfig.getConfigValue(), sysConfig.getConfigName(), sysConfig.getConfigType()))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 验重
        QueryWrapper<SysConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("configName", sysConfig.getConfigName()).or().eq("configKey", sysConfig.getConfigKey());
        List<SysConfig> list = sysConfigService.list(wrapper);
        if (list.size() > 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "配置名称或key重复");
        }
        boolean save = sysConfigService.save(sysConfig);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id修改参数配置
     * 不允许修改key
     * name不可重复
     *
     * @param sysConfigUpdateRequest
     * @return
     */
    @SaCheckRole(ADMIN_ROLE)
    @OpLog("更新参数配置:sys_config")
    @PutMapping("/config/update")
    public BaseResponse<Boolean> updateSysConfig(@RequestBody SysConfigUpdateRequest sysConfigUpdateRequest)
    {
        if (sysConfigUpdateRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判重
        QueryWrapper<SysConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("configName", sysConfigUpdateRequest.getConfigName());
        SysConfig one = sysConfigService.getOne(wrapper);
        if (one != null && !Objects.equals(sysConfigUpdateRequest.getId(), one.getId()))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "配置名称重复");
        }
        SysConfig sysConfig = new SysConfig();
        BeanUtils.copyProperties(sysConfigUpdateRequest, sysConfig);
        boolean update = sysConfigService.updateById(sysConfig);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id删除参数配置
     *
     * @param sysConfig
     * @return
     */
    @SaCheckRole(ADMIN_ROLE)
    @OpLog("删除参数配置:sys_config")
    @DeleteMapping("/config/delete")
    public BaseResponse<Boolean> deleteSysConfigById(@RequestBody SysConfig sysConfig)
    {
        if (sysConfig == null || sysConfig.getId() <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean remove = sysConfigService.removeById(sysConfig.getId());
        ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
