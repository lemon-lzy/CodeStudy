package sspu.zzx.sspuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import sspu.zzx.sspuoj.model.dto.organization.OrganizationQueryRequest;
import sspu.zzx.sspuoj.model.dto.organization.OrganizationSetupRequest;
import sspu.zzx.sspuoj.model.entity.Organization;
import sspu.zzx.sspuoj.model.vo.question.QuestionListVo;
import sspu.zzx.sspuoj.model.vo.user.UserVO;

import java.util.List;


/**
 * @author ZZX
 * @description 针对表【organization】的数据库操作Service
 * @createDate 2023-11-15 14:36:34
 */
public interface OrganizationService extends IService<Organization>
{

    /**
     * 获取Organization的查询条件
     *
     * @param organizationQueryRequest
     * @return
     */
    QueryWrapper<Organization> getQueryWrapper(OrganizationQueryRequest organizationQueryRequest);

    /**
     * 创建组织
     *
     * @param organizationSetupRequest
     * @return
     */
    Boolean setupOrganization(OrganizationSetupRequest organizationSetupRequest);

    /**
     * 更新组织信息
     *
     * @param organizationSetupRequest
     * @return
     */
    Boolean editOrganization(OrganizationSetupRequest organizationSetupRequest);

    /**
     * 申请加入组织
     *
     * @param organId
     * @return
     */
    Boolean joinOrganization(long organId);

    /**
     * 退出组织
     *
     * @param organId
     * @return
     */
    Boolean quitOrganization(long organId, long userId);

    /**
     * 根据状态查看组织内成员列表
     *
     * @param organId
     * @param status
     * @return
     */
    List<UserVO> lookMemberList(long organId, String status);

    /**
     * 处理组织内成员加入申请
     *
     * @param organId
     * @param userId
     * @param agreeIn
     * @return
     */
    Boolean handleApply(long organId, long userId, boolean agreeIn);

    /**
     * 拉黑/取消拉黑成员
     *
     * @param organId
     * @param userId
     * @return
     */
    Boolean blackMember(long organId, long userId);

    /**
     * 获取组织内问题列表
     *
     * @param organId
     * @return
     */
    List<QuestionListVo> lookQuestionListById(long organId);

    /**
     * 转让组织
     *
     * @param organId
     * @param beforeUserId
     * @param afterUserId
     * @return
     */
    Boolean transferOrganization(long organId, long beforeUserId, long afterUserId);

    /**
     * 解散组织
     *
     * @param organId
     * @return
     */
    Boolean destroyOrganization(long organId);
}
