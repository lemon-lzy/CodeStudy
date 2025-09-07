package com.lzy.codestudybackend.controller.recommend;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.codestudybackend.common.BaseResponse;
import com.lzy.codestudybackend.common.ResultUtils;
import com.lzy.codestudybackend.model.dto.recommend.QuestionRecommendRequest;
import com.lzy.codestudybackend.model.dto.recommend.UserRecommendRequest;
import com.lzy.codestudybackend.model.vo.QuestionRecommendVO;
import com.lzy.codestudybackend.model.vo.UserRecommendVO;
import com.lzy.codestudybackend.service.recommend.RecommendService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 推荐服务控制类
 */
@RestController
@RequestMapping("/recommend")
public class RecommendController {

    @Resource
    RecommendService recommendService;

    /**
     * 获取用户推荐
     */
    @PostMapping("/user/list")
    public BaseResponse<Page<UserRecommendVO>> getUserRecommendList(@RequestBody UserRecommendRequest request,
                                                                    HttpServletRequest httpServletRequest) {
        return ResultUtils.success(recommendService.getUserRecommendList(request));
    }

    /**
     * 获取题目推荐
     */
    @PostMapping("/question/list")
    public BaseResponse<Page<QuestionRecommendVO>> getQuestionRecommendList(@RequestBody QuestionRecommendRequest request,
                                                                            HttpServletRequest httpServletRequest) {
        return ResultUtils.success(recommendService.getQuestionRecommendList(request));
    }

}
