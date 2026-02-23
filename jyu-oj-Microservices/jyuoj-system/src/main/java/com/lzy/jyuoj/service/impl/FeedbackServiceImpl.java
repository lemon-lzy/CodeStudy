package sspu.zzx.sspuoj.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.constant.CommonConstant;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.mapper.FeedbackMapper;
import sspu.zzx.sspuoj.model.dto.feedback.FeedbackQueryRequest;
import sspu.zzx.sspuoj.model.entity.Feedback;
import sspu.zzx.sspuoj.model.entity.User;
import sspu.zzx.sspuoj.service.FeedbackService;
import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.service.impl.sys.StpInterfaceImpl;
import sspu.zzx.sspuoj.utils.SqlUtils;

import javax.annotation.Resource;
import java.util.List;

import static sspu.zzx.sspuoj.constant.UserConstant.ADMIN_ROLE;

/**
 * @author ZZX
 * @description 针对表【feedback】的数据库操作Service实现
 * @createDate 2023-11-15 14:35:26
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService
{
    @Resource
    private StpInterfaceImpl stpInterface;

    @Override
    public QueryWrapper<Feedback> getQueryWrapper(FeedbackQueryRequest feedbackQueryRequest)
    {
        if (feedbackQueryRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String applierName = feedbackQueryRequest.getApplierName();
        String handlerName = feedbackQueryRequest.getHandlerName();
        String applierContext = feedbackQueryRequest.getApplierContext();
        String type = feedbackQueryRequest.getType();
        String handleState = feedbackQueryRequest.getHandleState();
        String startTime = feedbackQueryRequest.getStartTime();
        String endTime = feedbackQueryRequest.getEndTime();
        String sortField = feedbackQueryRequest.getSortField();
        String sortOrder = feedbackQueryRequest.getSortOrder();

        QueryWrapper<Feedback> queryWrapper = new QueryWrapper<>();
        // 获取用户角色列表
        Long loginId = StpUtil.getLoginIdAsLong();
        List<String> roleList = stpInterface.getRoleList(loginId, "login");
        if (!roleList.contains(ADMIN_ROLE))
        {
            queryWrapper.eq(loginId != null, "applierId", loginId);
        }
        queryWrapper.like(StringUtils.isNotBlank(applierName), "applierName", applierName);
        queryWrapper.like(StringUtils.isNotBlank(handlerName), "handlerName", handlerName);
        queryWrapper.like(StringUtils.isNotBlank(applierContext), "applierContext", applierContext);
        queryWrapper.like(StringUtils.isNotBlank(type), "type", type);
        queryWrapper.like(StringUtils.isNotBlank(handleState), "handleState", handleState);
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime))
        {
            queryWrapper.between("createTime", startTime, endTime);
        }
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }
}




