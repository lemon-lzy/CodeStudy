package sspu.zzx.sspuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import sspu.zzx.sspuoj.model.dto.feedback.FeedbackQueryRequest;
import sspu.zzx.sspuoj.model.entity.Feedback;


/**
 * @author ZZX
 * @description 针对表【feedback】的数据库操作Service
 * @createDate 2023-11-15 14:35:26
 */
public interface FeedbackService extends IService<Feedback>
{

    /**
     * 获取查询条件
     *
     * @param feedbackQueryRequest
     * @return
     */
    QueryWrapper<Feedback> getQueryWrapper(FeedbackQueryRequest feedbackQueryRequest);
}
