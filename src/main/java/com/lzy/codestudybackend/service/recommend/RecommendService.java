package com.lzy.codestudybackend.service.recommend;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.codestudybackend.model.dto.recommend.QuestionRecommendRequest;
import com.lzy.codestudybackend.model.dto.recommend.UserRecommendRequest;
import com.lzy.codestudybackend.model.vo.QuestionRecommendVO;
import com.lzy.codestudybackend.model.vo.UserRecommendVO;

/**
 * 推荐服务类
 */
public interface RecommendService {
    /**
     * 获取用户推荐列表
     * @param request
     * @return
     */
    Page<UserRecommendVO> getUserRecommendList(UserRecommendRequest request);

    /**
     * 获取题目推荐列表
     * @param request 推荐请求
     * @return 分页后的推荐题目列表
     */
    Page<QuestionRecommendVO> getQuestionRecommendList(QuestionRecommendRequest request);
}
