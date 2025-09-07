package com.lzy.codestudybackend.service.interview;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lzy.codestudybackend.model.dto.interview.MockInterviewAddRequest;
import com.lzy.codestudybackend.model.dto.interview.MockInterviewEventRequest;
import com.lzy.codestudybackend.model.dto.interview.MockInterviewQueryRequest;
import com.lzy.codestudybackend.model.entity.interview.MockInterview;
import com.lzy.codestudybackend.model.entity.user.User;

/**

 * @description 针对表【mock_interview(模拟面试)】的数据库操作Service
 * @createDate 2025-02-26 17:16:07
 */
public interface MockInterviewService extends IService<MockInterview> {

    /**
     * 创建模拟面试
     *
     * @param mockInterviewAddRequest
     * @param loginUser
     * @return
     */
    Long createMockInterview(MockInterviewAddRequest mockInterviewAddRequest, User loginUser);

    /**
     * 构造查询条件
     *
     * @param mockInterviewQueryRequest
     * @return
     */
    QueryWrapper<MockInterview> getQueryWrapper(MockInterviewQueryRequest mockInterviewQueryRequest);

    /**
     * 处理模拟面试事件
     * @param mockInterviewEventRequest
     * @param loginUser
     * @return AI 给出的回复
     */
    String handleMockInterviewEvent(MockInterviewEventRequest mockInterviewEventRequest, User loginUser);
}
