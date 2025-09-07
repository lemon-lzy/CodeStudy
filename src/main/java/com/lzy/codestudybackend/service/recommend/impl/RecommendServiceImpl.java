package com.lzy.codestudybackend.service.recommend.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.codestudybackend.common.ErrorCode;
import com.lzy.codestudybackend.exception.BusinessException;
import com.lzy.codestudybackend.model.dto.recommend.QuestionRecommendRequest;
import com.lzy.codestudybackend.model.dto.recommend.UserRecommendRequest;
import com.lzy.codestudybackend.model.entity.question.QuestionCode;
import com.lzy.codestudybackend.model.entity.recommend.QuestionRecommend;
import com.lzy.codestudybackend.model.entity.recommend.UserRecommend;
import com.lzy.codestudybackend.model.entity.user.User;
import com.lzy.codestudybackend.model.vo.QuestionCodeVO;
import com.lzy.codestudybackend.model.vo.QuestionRecommendVO;
import com.lzy.codestudybackend.model.vo.UserRecommendVO;
import com.lzy.codestudybackend.model.vo.UserVO;
import com.lzy.codestudybackend.service.question.QuestionCodeService;
import com.lzy.codestudybackend.service.recommend.RecommendService;
import com.lzy.codestudybackend.service.recommend.algorithm.RecommendAlgorithm;
import com.lzy.codestudybackend.service.user.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
@Service
/**
 * 推荐服务实现类
 */
public class RecommendServiceImpl implements RecommendService {
    @Resource
    private RecommendAlgorithm recommendAlgorithm;
    @Resource
    private UserService userService;

    @Resource
    private QuestionCodeService questionCodeService;

    /**
     * 获取用户推荐（相似标签*0.5+工作经验相似度*0.3+活跃度*0.2）
     * @param request
     * @return
     */

    @Override
    public Page<UserRecommendVO> getUserRecommendList(UserRecommendRequest request) {
        Long userId = request.getUserId();

        // 1. 基于用户标签的协同过滤
        List<User> similarUsers = recommendAlgorithm.findSimilarUsers(userId);

        // 2. 计算推荐分数
        List<UserRecommend> recommendations = recommendAlgorithm.calculateUserScores(userId, similarUsers);

        // 3. 过滤并排序
        List<UserRecommend> filteredList = recommendations.stream()
                .filter(rec -> rec.getStatus() == 1)
                .sorted(Comparator.comparing(UserRecommend::getScore).reversed())
                .collect(Collectors.toList());

        // 4. 创建分页对象
        Page<UserRecommendVO> page = new Page<>(request.getCurrent(), request.getPageSize());

        // 5. 计算分页数据
        int start = (int) ((request.getCurrent() - 1) * request.getPageSize());
        int end = Math.min((int) (start + request.getPageSize()), filteredList.size());

        // 6. 获取分页后的数据
        List<UserRecommendVO> pageRecords = filteredList.subList(start, end)
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 7. 设置分页信息
        page.setRecords(pageRecords);
        page.setTotal(filteredList.size());

        return page;
    }
    // 解析用户
    private UserRecommendVO convertToVO(UserRecommend userRecommend) {
        UserRecommendVO vo = new UserRecommendVO();
        BeanUtils.copyProperties(userRecommend, vo);

        // 解析标签
        if (userRecommend.getTags() != null) {
            vo.setTags(JSONObject.parseArray(userRecommend.getTags(), String.class));
        }
        User user = userService.getById(userRecommend.getRecommendUserId());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        vo.setRecommendUser(userVO);

        return vo;
    }


    /**
     * 计算推荐算法题
     * @param request 推荐请求
     * @return
     */
    @Override
    public Page<QuestionRecommendVO> getQuestionRecommendList(QuestionRecommendRequest request) {
        Long userId = request.getUserId();
        String type = request.getType();

        List<QuestionRecommend> recommendations;
        switch (type) {
            case "similar":
                recommendations = recommendAlgorithm.getSimilarQuestions(userId);
                break;
            case "daily":
                recommendations = recommendAlgorithm.getDailyRecommendations(userId);
                break;
            case "level":
                recommendations = recommendAlgorithm.getLevelProgressions(userId);
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 对推荐列表进行排序
        recommendations = recommendations.stream()
                .sorted(Comparator.comparing(QuestionRecommend::getScore).reversed())
                .collect(Collectors.toList());

        // 创建分页对象
        Page<QuestionRecommendVO> page = new Page<>(request.getCurrent(), request.getPageSize());

        // 计算分页数据
        int start = (int) ((request.getCurrent() - 1) * request.getPageSize());
        int end = Math.min((int) (start + request.getPageSize()), recommendations.size());

        // 获取分页后的数据
        List<QuestionRecommendVO> pageRecords = recommendations.subList(start, end)
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 设置分页信息
        page.setRecords(pageRecords);
        page.setTotal(recommendations.size());

        return page;
    }

    //解析题目
    private QuestionRecommendVO convertToVO(QuestionRecommend questionRecommend) {
        QuestionRecommendVO vo = new QuestionRecommendVO();
        BeanUtils.copyProperties(questionRecommend, vo);

        QuestionCode questionCode = questionCodeService.getById(questionRecommend.getQuestionId());
        QuestionCodeVO questionCodeVO = new QuestionCodeVO();
        BeanUtils.copyProperties(questionCode, questionCodeVO);
        if (questionCode.getTags() != null) {
            questionCodeVO.setTags(JSONObject.parseArray(questionCode.getTags(), String.class));
        }
        vo.setQuestionCodeVO(questionCodeVO);
        return vo;
    }

}
