package sspu.zzx.sspuoj.controller;

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
import sspu.zzx.sspuoj.model.dto.feedback.FeedbackQueryRequest;
import sspu.zzx.sspuoj.model.entity.Feedback;
import sspu.zzx.sspuoj.model.enums.StateEnum;
import sspu.zzx.sspuoj.service.FeedbackService;

import javax.annotation.Resource;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/11/21 14:20
 */
@RestController
@RequestMapping("/feedback")
@Slf4j
public class FeedbackController
{
    @Resource
    private FeedbackService feedbackService;

    /**
     * 分页检索用户对应的反馈
     *
     * @param feedbackQueryRequest
     * @return
     */
    @PostMapping("/query")
    public BaseResponse<Page<Feedback>> getUserFeedbackList(@RequestBody FeedbackQueryRequest feedbackQueryRequest)
    {
        long current = feedbackQueryRequest.getCurrent();
        long size = feedbackQueryRequest.getPageSize();
        Page<Feedback> feedbackPage = feedbackService.page(new Page<>(current, size), feedbackService.getQueryWrapper(feedbackQueryRequest));
        return ResultUtils.success(feedbackPage);
    }

    /**
     * 新增反馈，用户提交的type默认为【常规】
     *
     * @param feedback
     * @return
     */
    @OpLog("新增反馈:feedback")
    @PostMapping("/add")
    public BaseResponse<Boolean> addFeedBack(@RequestBody Feedback feedback)
    {
        // 反馈内容、id、name和反馈类型不得为空
        if (feedback == null || StringUtils.isAnyBlank(feedback.getApplierContext(), feedback.getType(), feedback.getApplierName()) || feedback.getApplierId() == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 赋初值，添加
        feedback.setHandleState(StateEnum.UN_HANDLED.getValue());
        boolean save = feedbackService.save(feedback);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id修改反馈
     *
     * @param feedback
     * @return
     */
    @OpLog("修改反馈:feedback")
    @PutMapping("/update")
    public BaseResponse<Boolean> updateFeedBack(@RequestBody Feedback feedback)
    {
        if (feedback == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean update = feedbackService.updateById(feedback);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除（已处理）的反馈
     *
     * @param feedback
     * @return
     */
    @OpLog("删除反馈(已处理):feedback")
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteFeedBackById(@RequestBody Feedback feedback)
    {
        if (feedback == null || feedback.getId() <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检查状态
        Feedback byId = feedbackService.getById(feedback.getId());
        if (byId == null || StateEnum.UN_HANDLED.equals(byId.getHandleState()))
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能删除未处理的申请！");
        }
        boolean remove = feedbackService.removeById(feedback.getId());
        ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


}
