package sspu.zzx.sspuoj.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.constant.CommonConstant;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.mapper.*;
import sspu.zzx.sspuoj.model.dto.organization.OrganizationQueryRequest;
import sspu.zzx.sspuoj.model.dto.organization.OrganizationSetupRequest;
import sspu.zzx.sspuoj.model.entity.*;
import sspu.zzx.sspuoj.model.enums.StateEnum;
import sspu.zzx.sspuoj.model.vo.question.QuestionListVo;
import sspu.zzx.sspuoj.model.vo.user.UserVO;
import sspu.zzx.sspuoj.service.OrganizationService;
import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.service.QuestionListService;
import sspu.zzx.sspuoj.service.UserService;
import sspu.zzx.sspuoj.utils.SqlUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ZZX
 * @description 针对表【organization】的数据库操作Service实现
 * @createDate 2023-11-15 14:36:34
 */
@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService
{
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private FeedbackMapper feedbackMapper;
    @Resource
    private UserOrganMapper userOrganMapper;
    @Resource
    private UserService userService;
    @Resource
    private QuestionListService questionListService;
    @Resource
    private GameMapper gameMapper;
    @Resource
    private UserGameMapper userGameMapper;


    @Override
    public QueryWrapper<Organization> getQueryWrapper(OrganizationQueryRequest organizationQueryRequest)
    {
        QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
        if (organizationQueryRequest == null)
        {
            return queryWrapper;
        }
        Long id = organizationQueryRequest.getId();
        String organName = organizationQueryRequest.getOrganName();
        Long organizerId = organizationQueryRequest.getOrganizerId();
        String organizerName = organizationQueryRequest.getOrganizerName();
        String sortField = organizationQueryRequest.getSortField();
        String sortOrder = organizationQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(organizerId), "organizerId", organizerId);
        queryWrapper.like(StringUtils.isNotBlank(organName), "organName", organName);
        queryWrapper.like(StringUtils.isNotBlank(organizerName), "organizerName", organizerName);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);

        return queryWrapper;
    }

    @Override
    public Boolean setupOrganization(OrganizationSetupRequest organizationSetupRequest)
    {
        // 校验参数
        validOrganizationSetupRequest(organizationSetupRequest);
        // 检查当前用户是否已有组织
        checkUserOrgan();
        //  创建组织
        Organization organization = new Organization();
        BeanUtils.copyProperties(organizationSetupRequest, organization);
        User loginUser = userService.getLoginUser(null);
        organization.setOrganizerId(loginUser.getId());
        organization.setOrganizerName(loginUser.getUserName());
        organization.setOrganCurrentNum(1);
        int insert = organizationMapper.insert(organization);
        if (insert > 0)
        {
            // 同步至userOrgan表
            UserOrgan userOrgan = new UserOrgan();
            userOrgan.setUserId(loginUser.getId());
            userOrgan.setOrganId(organization.getId());
            userOrgan.setStatus(StateEnum.ORGAN_IN.getValue());
            userOrganMapper.insert(userOrgan);
        }
        return insert > 0;
    }

    @Override
    public Boolean editOrganization(OrganizationSetupRequest organizationSetupRequest)
    {
        if (organizationSetupRequest.getId() == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "组织id不能为空");
        }
        Organization organization = new Organization();
        BeanUtils.copyProperties(organizationSetupRequest, organization);
        // 检验组织最大人数上限是否合法
        if (organization.getOrganTotalNum() != null)
        {
            Organization organization1 = organizationMapper.selectById(organization.getId());
            if (organization1.getOrganCurrentNum() > organization.getOrganTotalNum())
            {
                throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "组织人数上限不能小于当前人数");
            }
        }
        int update = organizationMapper.updateById(organization);
        return update > 0;
    }

    public void validOrganizationSetupRequest(OrganizationSetupRequest organizationSetupRequest)
    {
        if (StringUtils.isAnyBlank(organizationSetupRequest.getOrganAvatar(), organizationSetupRequest.getOrganName()) || ObjectUtils.isEmpty(organizationSetupRequest.getOrganTotalNum()) || organizationSetupRequest.getOrganTotalNum() <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    @Override
    public Boolean joinOrganization(long organId)
    {
        // 检查自己是否已有组织
        checkUserOrgan();
        // 检查自己被拉黑
        QueryWrapper<UserOrgan> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", StpUtil.getLoginIdAsLong());
        wrapper.eq("organId", organId);
        UserOrgan userOrgan = userOrganMapper.selectOne(wrapper);
        if (userOrgan != null && StateEnum.ORGAN_BLACK.getValue().equals(userOrgan.getStatus()))
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "您已被拉黑，无法加入该组织");
        }
        // 检查自己是否已提交过申请
        QueryWrapper<UserOrgan> userOrganQueryWrapper = new QueryWrapper<>();
        userOrganQueryWrapper.eq("userId", StpUtil.getLoginIdAsLong());
        userOrganQueryWrapper.eq("status", StateEnum.ORGAN_ING.getValue());
        List<UserOrgan> userOrgans = userOrganMapper.selectList(userOrganQueryWrapper);
        if (userOrgans != null && userOrgans.size() > 0)
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "您已提交过申请，请等待审核");
        }
        // 检查组织人数是否已满
        Organization organization = organizationMapper.selectById(organId);
        if (organization.getOrganTotalNum() <= organization.getOrganCurrentNum())
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "组织人数已满，无法加入该组织");
        }
        // 添加加入组织申请
        UserOrgan userOrgan1 = new UserOrgan();
        userOrgan1.setOrganId(organId);
        userOrgan1.setUserId(StpUtil.getLoginIdAsLong());
        userOrgan1.setStatus(StateEnum.ORGAN_ING.getValue());
        int insert = userOrganMapper.insert(userOrgan1);
        // 同步至反馈表方便查看消息
        User loginUser = userService.getLoginUser(null);
        Feedback feedback = new Feedback();
        feedback.setHandleState(StateEnum.UN_HANDLED.getValue());
        feedback.setApplierContext("申请加入组织:" + organization.getOrganName());
        feedback.setApplierId(loginUser.getId());
        feedback.setApplierName(loginUser.getUserName());
        feedback.setType("常规");
        feedbackMapper.insert(feedback);
        return insert > 0;
    }

    @Override
    public Boolean quitOrganization(long organId, long userId)
    {
        // 判断自己是否是创建者，如果是则失败
        Organization organization = organizationMapper.selectById(organId);
        // long loginUserId = StpUtil.getLoginIdAsLong();
        long loginUserId = userId;
        if (organization.getOrganizerId().equals(loginUserId))
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "创建者无法退出组织，请先转让组织");
        }
        // 退出组织
        QueryWrapper<UserOrgan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("organId", organId).eq("userId", loginUserId);
        int delete = userOrganMapper.delete(queryWrapper);
        // 更新组织人数
        organization.setOrganCurrentNum(organization.getOrganCurrentNum() - 1);
        organizationMapper.updateById(organization);
        return delete > 0;
    }

    @Override
    public List<UserVO> lookMemberList(long organId, String status)
    {
        if (StringUtils.isBlank(status))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserOrgan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("organId", organId).eq("status", status);
        List<UserOrgan> userOrganList = userOrganMapper.selectList(queryWrapper);
        List<Long> userIds = userOrganList.stream().map(UserOrgan::getUserId).distinct().collect(Collectors.toList());
        return userService.getUserVOByUserIdList(userIds);
    }

    @Override
    public Boolean handleApply(long organId, long userId, boolean agreeIn)
    {
        Organization organization = organizationMapper.selectById(organId);
        // 同意加入该组织
        if (agreeIn)
        {
            // 查询当前组织人数
            if (organization.getOrganTotalNum() <= organization.getOrganCurrentNum())
            {
                throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "组织人数已满");
            }
            // 更新组织人数
            organization.setOrganCurrentNum(organization.getOrganCurrentNum() + 1);
            organizationMapper.updateById(organization);
            // 添加组织成员
            QueryWrapper<UserOrgan> wrapper = new QueryWrapper<>();
            wrapper.eq("organId", organId).eq("userId", userId).eq("status", StateEnum.ORGAN_ING.getValue());
            UserOrgan userOrgan = userOrganMapper.selectOne(wrapper);
            if (userOrgan == null)
            {
                throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "用户申请信息不存在");
            }
            userOrgan.setStatus(StateEnum.ORGAN_IN.getValue());
            userOrganMapper.updateById(userOrgan);
            // 更新组织成员反馈信息
            QueryWrapper<Feedback> feedbackWrapper = new QueryWrapper<>();
            feedbackWrapper.eq("applierId", userId).eq("applierContext", "申请加入组织:" + organization.getOrganName());
            feedbackWrapper.eq("type", "常规").eq("handleState", StateEnum.UN_HANDLED.getValue());
            Feedback feedback = feedbackMapper.selectOne(feedbackWrapper);
            if (feedback != null)
            {
                User loginUser = userService.getLoginUser(null);
                feedback.setHandleState(StateEnum.HANDLED.getValue());
                feedback.setHandlerId(loginUser.getId());
                feedback.setHandlerName(loginUser.getUserName());
                feedback.setHandleResult("同意");
                feedbackMapper.updateById(feedback);
            }
        }
        // 拒绝
        else
        {
            QueryWrapper<UserOrgan> wrapper2 = new QueryWrapper<>();
            wrapper2.eq("organId", organId).eq("userId", userId).eq("status", StateEnum.ORGAN_ING.getValue());
            userOrganMapper.delete(wrapper2);
            // 更新组织成员反馈信息
            QueryWrapper<Feedback> feedbackWrapper = new QueryWrapper<>();
            feedbackWrapper.eq("applierId", userId).eq("applierContext", "申请加入组织:" + organization.getOrganName());
            feedbackWrapper.eq("type", "常规").eq("handleState", StateEnum.UN_HANDLED.getValue());
            Feedback feedback = feedbackMapper.selectOne(feedbackWrapper);
            if (feedback != null)
            {
                User loginUser = userService.getLoginUser(null);
                feedback.setHandleState(StateEnum.HANDLED.getValue());
                feedback.setHandlerId(loginUser.getId());
                feedback.setHandlerName(loginUser.getUserName());
                feedback.setHandleResult("拒绝");
                feedbackMapper.updateById(feedback);
            }
        }
        return true;
    }

    @Override
    public Boolean blackMember(long organId, long userId)
    {
        QueryWrapper<UserOrgan> wrapper = new QueryWrapper<>();
        wrapper.eq("organId", organId).eq("userId", userId);
        UserOrgan userOrgan = userOrganMapper.selectOne(wrapper);
        if (userOrgan == null)
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "用户申请信息不存在");
        }
        // 拉黑
        if (StateEnum.ORGAN_ING.getValue().equals(userOrgan.getStatus()))
        {
            userOrgan.setStatus(StateEnum.ORGAN_BLACK.getValue());
            userOrganMapper.updateById(userOrgan);
            // 更新反馈信息
            QueryWrapper<Feedback> feedbackWrapper = new QueryWrapper<>();
            Organization organization = organizationMapper.selectById(organId);
            feedbackWrapper.eq("applierId", userId).eq("applierContext", "申请加入组织:" + organization.getOrganName());
            feedbackWrapper.eq("type", "常规").eq("handleState", StateEnum.UN_HANDLED.getValue());
            Feedback feedback = feedbackMapper.selectOne(feedbackWrapper);
            if (feedback != null)
            {
                User loginUser = userService.getLoginUser(null);
                feedback.setHandleState(StateEnum.HANDLED.getValue());
                feedback.setHandlerId(loginUser.getId());
                feedback.setHandlerName(loginUser.getUserName());
                feedback.setHandleResult("被拉黑");
                feedbackMapper.updateById(feedback);
            }
        }
        // 取消拉黑
        else
        {
            // 这里删除是最好的选择
            userOrganMapper.deleteById(userOrgan);
        }
        return true;
    }

    @Override
    public List<QuestionListVo> lookQuestionListById(long organId)
    {
        Organization organization = organizationMapper.selectById(organId);
        Long organizerId = organization.getOrganizerId();
        QueryWrapper<QuestionList> wrapper = new QueryWrapper<>();
        wrapper.eq("creatorId", organizerId);
        List<QuestionList> questionLists = questionListService.list(wrapper);
        List<QuestionListVo> questionListVOList = questionListService.getQuestionListVOList(questionLists);
        return questionListVOList;
    }

    @Override
    public Boolean transferOrganization(long organId, long beforeUserId, long afterUserId)
    {
        // 不能自己转让给自己
        if (beforeUserId == afterUserId)
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "当前成员已经是群主，无法转让");
        }
        // 获取当前组织记录
        Organization organization = organizationMapper.selectById(organId);
        // 获取目标群主记录
        User afterUser = userService.getById(afterUserId);
        // 更新组织群主
        organization.setOrganizerId(afterUserId);
        organization.setOrganizerName(afterUser.getUserName());
        organizationMapper.updateById(organization);
        // 更新题单归属
        QueryWrapper<QuestionList> wrapper = new QueryWrapper<>();
        wrapper.eq("creatorId", beforeUserId);
        List<QuestionList> questionLists = questionListService.list(wrapper);
        for (QuestionList questionList : questionLists)
        {
            questionList.setCreatorId(afterUserId);
            questionList.setCreatorName(afterUser.getUserName());
        }
        questionListService.updateBatchById(questionLists);
        // 更新竞赛归属（如果被转让的人未开启（加入）竞赛，则使其加入）
        QueryWrapper<Game> gameQueryWrapper = new QueryWrapper<>();
        gameQueryWrapper.eq("gameFounderId", beforeUserId);
        gameQueryWrapper.eq("publicZone", "private");
        List<Game> games = gameMapper.selectList(gameQueryWrapper);
        for (Game game : games)
        {
            game.setGameFounderId(afterUserId);
            game.setGameFounderName(afterUser.getUserName());
            gameMapper.updateById(game);
            QueryWrapper<UserGame> userGameQueryWrapper = new QueryWrapper<>();
            userGameQueryWrapper.eq("gameId", game.getId()).eq("userId", afterUserId);
            UserGame userGame = userGameMapper.selectOne(userGameQueryWrapper);
            if (userGame == null)
            {
                userGame = new UserGame();
                userGame.setGameId(game.getId());
                userGame.setUserId(afterUserId);
                userGameMapper.insert(userGame);
            }
        }

        return true;
    }

    @Override
    public Boolean destroyOrganization(long organId)
    {
        Organization organization = organizationMapper.selectById(organId);
        Integer organCurrentNum = organization.getOrganCurrentNum();
        if (organCurrentNum == null || organCurrentNum > 1)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只能解散无其他成员的组织哦！");
        }
        Long organizationId = organization.getId();
        organizationMapper.deleteById(organizationId);
        QueryWrapper<UserOrgan> userOrganQueryWrapper = new QueryWrapper<>();
        userOrganQueryWrapper.eq("organId", organizationId);
        userOrganMapper.delete(userOrganQueryWrapper);

        return true;
    }

    public void checkUserOrgan()
    {
        QueryWrapper<UserOrgan> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", StpUtil.getLoginIdAsLong());
        wrapper.eq("status", StateEnum.ORGAN_IN.getValue());
        UserOrgan userOrgan = userOrganMapper.selectOne(wrapper);
        if (ObjectUtils.isNotEmpty(userOrgan))
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前用户已有组织，无法执行当前操作");
        }
    }
}




