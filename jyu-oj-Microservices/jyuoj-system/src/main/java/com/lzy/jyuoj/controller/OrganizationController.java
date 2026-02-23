package sspu.zzx.sspuoj.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import sspu.zzx.sspuoj.aop.annotation.OpLog;
import sspu.zzx.sspuoj.common.BaseResponse;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.common.ResultUtils;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.exception.ThrowUtils;
import sspu.zzx.sspuoj.model.dto.organization.OrganizationQueryRequest;
import sspu.zzx.sspuoj.model.dto.organization.OrganizationSetupRequest;
import sspu.zzx.sspuoj.model.entity.Organization;
import sspu.zzx.sspuoj.model.entity.UserOrgan;
import sspu.zzx.sspuoj.model.vo.question.QuestionListVo;
import sspu.zzx.sspuoj.model.vo.user.UserVO;
import sspu.zzx.sspuoj.service.OrganizationService;
import sspu.zzx.sspuoj.service.UserOrganService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/12/28 10:51
 */
@RestController
@RequestMapping("/organization")
@Slf4j
public class OrganizationController
{
    @Resource
    private OrganizationService organizationService;
    @Resource
    private UserOrganService userOrganService;

    /**
     * 根据id或其他条件获取组织信息（分页）
     *
     * @param organizationQueryRequest
     * @return
     */
    @PostMapping("list/page")
    public BaseResponse<Page<Organization>> getOrganizationListPage(@RequestBody OrganizationQueryRequest organizationQueryRequest)
    {
        long current = organizationQueryRequest.getCurrent();
        long size = organizationQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Organization> organizationPage = organizationService.page(new Page<>(current, size), organizationService.getQueryWrapper(organizationQueryRequest));
        return ResultUtils.success(organizationPage);
    }

    /**
     * 获取用户组织信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/organ/{userId}")
    public BaseResponse<UserOrgan> getOnesOrganization(@PathVariable long userId)
    {
        QueryWrapper<UserOrgan> userOrganQueryWrapper = new QueryWrapper<>();
        userOrganQueryWrapper.eq("userId", userId);
        userOrganQueryWrapper.eq("status", "在职");
        UserOrgan userOrgan = userOrganService.getOne(userOrganQueryWrapper);
        return ResultUtils.success(userOrgan);
    }

    /**
     * 创建组织（只能创建或加入一个）
     *
     * @param organizationSetupRequest
     * @return
     */
    @OpLog("创建组织:organization")
    @PostMapping("/setup")
    @SaCheckPermission(value = {"create.organization"}, orRole = "admin")
    public BaseResponse<Boolean> setupOrganization(@RequestBody OrganizationSetupRequest organizationSetupRequest)
    {
        if (organizationSetupRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(organizationService.setupOrganization(organizationSetupRequest));
    }


    /**
     * 更新组织信息
     *
     * @param organizationSetupRequest
     * @return
     */
    @OpLog("更新组织信息:organization")
    @PutMapping("/edit")
    public BaseResponse<Boolean> editOrganization(@RequestBody OrganizationSetupRequest organizationSetupRequest)
    {
        if (organizationSetupRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(organizationService.editOrganization(organizationSetupRequest));
    }


    /**
     * 加入组织（只能创建或加入一个）
     *
     * @param organId
     * @return
     */
    @OpLog("加入组织:organization")
    @PostMapping("/join")
    public BaseResponse<Boolean> joinOrganization(@RequestParam("organId") long organId)
    {
        return ResultUtils.success(organizationService.joinOrganization(organId));
    }


    /**
     * 退出组织（包括主动退出和管理员剔除成员）
     *
     * @param organId
     * @return
     */
    @OpLog("退出组织:organization")
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitOrganization(@RequestParam("organId") long organId, @RequestParam("userId") long userId)
    {
        return ResultUtils.success(organizationService.quitOrganization(organId, userId));
    }

    /**
     * 查看组织的成员列表（在职、申请中、拉黑）
     * 1. 在职：组织内成员
     * 2. 申请中：申请加入组织的成员
     * 3. 拉黑：不允许加入组织的成员
     *
     * @param organId
     * @param status
     * @return
     */
    @GetMapping("lookMemberList")
    public BaseResponse<List<UserVO>> lookMemberList(@RequestParam("organId") long organId, @RequestParam("status") String status)
    {
        if (StringUtils.isBlank(status))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(organizationService.lookMemberList(organId, status));
    }

    /**
     * 处理加入组织申请
     *
     * @param organId
     * @param userId
     * @param agreeIn
     * @return
     */
    @OpLog("处理加入组织申请:organization")
    @PostMapping("handleApply")
    public BaseResponse<Boolean> handleApply(@RequestParam("organId") long organId, @RequestParam("userId") long userId, @RequestParam("agreeIn") boolean agreeIn)
    {
        return ResultUtils.success(organizationService.handleApply(organId, userId, agreeIn));
    }

    /**
     * 拉黑/取消拉黑成员
     *
     * @param organId
     * @param userId
     * @return
     */
    @OpLog("拉黑/取消拉黑成员:organization")
    @PostMapping("/blackMember")
    public BaseResponse<Boolean> blackMember(@RequestParam("organId") long organId, @RequestParam("userId") long userId)
    {
        return ResultUtils.success(organizationService.blackMember(organId, userId));
    }

    /**
     * 查看组织的题单列表
     *
     * @param organId
     * @return
     */
    @GetMapping("lookQuestionListById")
    public BaseResponse<List<QuestionListVo>> lookQuestionListById(@RequestParam("organId") long organId)
    {
        return ResultUtils.success(organizationService.lookQuestionListById(organId));
    }

    /**
     * 组织转让
     * 提示：转让后，对应的题单也会转移
     *
     * @param organId
     * @param beforeUserId
     * @param afterUserId
     * @return
     */
    @OpLog("群主转让:organization")
    @PostMapping("/transferOrganization")
    public BaseResponse<Boolean> transferOrganization(@RequestParam("organId") long organId, @RequestParam("beforeUserId") long beforeUserId, @RequestParam("afterUserId") long afterUserId)
    {
        return ResultUtils.success(organizationService.transferOrganization(organId, beforeUserId, afterUserId));
    }

    /**
     * 解散组织（仅能解散除自己外没人加入的组织）
     *
     * @param organId
     * @return
     */
    @OpLog("解散组织:organization")
    @DeleteMapping("destroyOrganization")
    public BaseResponse<Boolean> destroyOrganization(@RequestParam("organId") long organId)
    {
        return ResultUtils.success(organizationService.destroyOrganization(organId));
    }
}
