package com.lzy.codestudybackend.service.question.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.codestudybackend.common.ErrorCode;
import com.lzy.codestudybackend.constant.CommonConstant;
import com.lzy.codestudybackend.exception.BusinessException;
import com.lzy.codestudybackend.mapper.question.QuestionListMapper;
import com.lzy.codestudybackend.mapper.question.QuestionUnionMapper;
import com.lzy.codestudybackend.model.dto.questionlist.QuestionListAddRequest;
import com.lzy.codestudybackend.model.dto.questionlist.QuestionListIdToCount;
import com.lzy.codestudybackend.model.dto.questionlist.QuestionListProfile;
import com.lzy.codestudybackend.model.dto.questionlist.QuestionListQueryRequest;
import com.lzy.codestudybackend.model.entity.question.Question;
import com.lzy.codestudybackend.model.entity.question.QuestionList;
import com.lzy.codestudybackend.model.entity.question.QuestionUnion;
import com.lzy.codestudybackend.model.entity.user.User;
import com.lzy.codestudybackend.model.vo.QuestionListVo;
import com.lzy.codestudybackend.model.vo.QuestionVO;
import com.lzy.codestudybackend.service.question.QuestionListService;
import com.lzy.codestudybackend.service.question.QuestionService;
import com.lzy.codestudybackend.service.user.UserService;
import com.lzy.codestudybackend.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lzy
 * @description 针对表【question_list】的数据库操作Service实现
 * @createDate 2023-11-15 14:36:40
 */
@Service
public class QuestionListServiceImpl extends ServiceImpl<QuestionListMapper, QuestionList> implements QuestionListService
{
    @Resource
    private QuestionListMapper questionListMapper;
    @Resource
    private QuestionUnionMapper questionUnionMapper;
    @Resource
    private QuestionService questionService;
    @Resource
    private UserService userService;


    @Override
    public Page<QuestionListVo> getQuestionListVOPage(Page<QuestionList> questionListPage)
    {
        List<QuestionList> questionLists = questionListPage.getRecords();
        Page<QuestionListVo> questionListVoPage = new Page<>(questionListPage.getCurrent(), questionListPage.getSize(), questionListPage.getTotal());
        if (CollectionUtils.isEmpty(questionLists))
        {
            return questionListVoPage;
        }
        List<QuestionListVo> questionListVos = getQuestionListVOList(questionLists);
        questionListVoPage.setRecords(questionListVos);
        return questionListVoPage;
    }

    @Override
    public List<QuestionListVo> getQuestionListVOList(List<QuestionList> questionLists)
    {
        if (questionLists == null || questionLists.isEmpty())
        {
            return new ArrayList<>();
        }
        List<QuestionListVo> questionListVos = questionLists.stream().map(e ->
        {
            QuestionListVo questionListVo = new QuestionListVo();
            BeanUtils.copyProperties(e, questionListVo);
            return questionListVo;
        }).collect(Collectors.toList());
        // 查询题单中题目的数量
        List<Long> listIds = questionListVos.stream().map(QuestionListVo::getId).collect(Collectors.toList());
        List<QuestionListIdToCount> questionCountList = questionUnionMapper.getQuestionCountByListIds(listIds);
        Map<Long, Long> questionCountByListIds = questionCountList.stream().collect(Collectors.toMap(QuestionListIdToCount::getListId, QuestionListIdToCount::getQuestionCount));
        questionListVos.forEach(e -> e.setQuestionCount(questionCountByListIds.get(e.getId())));
        return questionListVos;
    }

    @Override
    public QueryWrapper<QuestionList> getQueryWrapper(QuestionListQueryRequest questionListQueryRequest)
    {
        QueryWrapper<QuestionList> queryWrapper = new QueryWrapper<>();
        if (questionListQueryRequest == null)
        {
            return queryWrapper;
        }
        Long id = questionListQueryRequest.getId();
        String listName = questionListQueryRequest.getListName();
        Long creatorId = questionListQueryRequest.getCreatorId();
        String creatorName = questionListQueryRequest.getCreatorName();
        String publicZone = questionListQueryRequest.getPublicZone();
        String sortField = questionListQueryRequest.getSortField();
        String sortOrder = questionListQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(creatorId), "creatorId", creatorId);
        queryWrapper.like(StringUtils.isNotBlank(creatorName), "creatorName", creatorName);
        queryWrapper.like(StringUtils.isNotBlank(listName), "listName", listName);
        queryWrapper.eq(StringUtils.isNotBlank(publicZone), "publicZone", publicZone);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);

        return queryWrapper;
    }

    @Override
    public List<QuestionVO> getQuestionVOList(Long questionListId)
    {
        // 根据题单id获取内含的题目集合
        List<Question> questionList = questionUnionMapper.getQuestionsById(questionListId);
        // 返回题目封装类集合
        return questionService.getQuestionVOList(questionList, true);
    }

    @Override
    public Boolean addQuestionList(QuestionListAddRequest questionListAddRequest)
    {
        // 参数校验
        validAddRequest(questionListAddRequest);
        //复制基本信息
        QuestionListProfile listProfile = questionListAddRequest.getListProfile();
        Long creatorId = questionListAddRequest.getCreatorId();
        List<Long> questionIdList = questionListAddRequest.getQuestionIdList();
        QuestionList questionList = new QuestionList();
        BeanUtils.copyProperties(questionListAddRequest, questionList);
        // 如果创建者id为空，则系统自动获取当前登录用户
        if (creatorId == null)
        {
            User loginUser = userService.getLoginUser(null);
            questionList.setCreatorId(loginUser.getId());
            questionList.setCreatorName(loginUser.getUserName());
        }
        // 处理简介对象为json字符串
        questionList.setListProfile(JSONUtil.toJsonStr(listProfile));
        // 插入题单
        int insert = questionListMapper.insert(questionList);
        if (insert > 0)
        {
            // 插入题单题目关联表 插入题单题目关联表
            for (Long questionId : questionIdList)
            {
                QuestionUnion questionUnion = new QuestionUnion();
                questionUnion.setQuestionId(questionId);
                questionUnion.setListId(questionList.getId());
                questionUnionMapper.insert(questionUnion);
            }
        }
        return insert > 0;
    }

    @Override
    public Boolean editQuestionList(QuestionListAddRequest questionListAddRequest)
    {
        // 题单id不得为空
        if (questionListAddRequest.getId() == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题单id不得为空");
        }
        // 参数校验
        validAddRequest(questionListAddRequest);
        // 修改题单基本信息
        QuestionList questionList = new QuestionList();
        BeanUtils.copyProperties(questionListAddRequest, questionList);
        questionList.setListProfile(JSONUtil.toJsonStr(questionListAddRequest.getListProfile()));
        questionListMapper.updateById(questionList);
        // 修改题目列表信息
        // 1. 获取当前数据库中的题目列表
        QueryWrapper<QuestionUnion> unionQueryWrapper = new QueryWrapper<>();
        unionQueryWrapper.eq("listId", questionListAddRequest.getId());
        List<QuestionUnion> questionUnions = questionUnionMapper.selectList(unionQueryWrapper);
        // 2. 对比两个版本的题单，找到要删除和要添加的题目
        List<Long> list0 = questionUnions.stream().map(QuestionUnion::getQuestionId).collect(Collectors.toList());
        List<Long> list1 = questionListAddRequest.getQuestionIdList();
        List<Long> toDeleteIds = new ArrayList<>();
        for (int i = 0; i < list0.size(); i++)
        {
            Long questionId = list0.get(i);
            if (!list1.contains(questionId))
            {
                toDeleteIds.add(questionUnions.get(i).getId());
            }
        }
        List<Long> toAddQuestionIds = new ArrayList<>();
        for (Long questionId : list1)
        {
            if (!list0.contains(questionId))
            {
                toAddQuestionIds.add(questionId);
            }
        }
        // 3. 删除题目
        if (toDeleteIds.size() > 0)
        {
            questionUnionMapper.deleteBatchIds(toDeleteIds);
        }
        // 4. 添加题目
        for (Long toAddId : toAddQuestionIds)
        {
            QuestionUnion questionUnion = new QuestionUnion();
            questionUnion.setListId(questionListAddRequest.getId());
            questionUnion.setQuestionId(toAddId);
            questionUnionMapper.insert(questionUnion);
        }
        return true;
    }

    public void validAddRequest(QuestionListAddRequest questionListAddRequest)
    {
        // 题单名不得为空
        if (StringUtils.isBlank(questionListAddRequest.getListName()))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题单名不得为空");
        }
        // 题单公开区域不得为空
        if (StringUtils.isBlank(questionListAddRequest.getPublicZone()))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题单公开区域不得为空");
        }
        // 题目列表不得为空
        if (questionListAddRequest.getQuestionIdList() == null || questionListAddRequest.getQuestionIdList().size() == 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目列表不得为空");
        }
        // 题目简介为空则创建
        if (questionListAddRequest.getListProfile() == null)
        {
            QuestionListProfile questionListProfile = new QuestionListProfile();
            questionListProfile.setListDescription("简介暂时为空~");
            questionListAddRequest.setListProfile(questionListProfile);
        }
    }
}




