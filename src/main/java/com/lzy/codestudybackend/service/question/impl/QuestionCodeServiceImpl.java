package com.lzy.codestudybackend.service.question.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.codestudybackend.mapper.question.QuestionCodeMapper;
import com.lzy.codestudybackend.model.entity.question.QuestionCode;
import com.lzy.codestudybackend.service.question.QuestionCodeService;
import org.springframework.stereotype.Service;

/**
* @author xier
* @description 针对表【question_code(算法题)】的数据库操作Service实现
* @createDate 2025-08-30 18:19:05
*/
@Service
public class QuestionCodeServiceImpl extends ServiceImpl<QuestionCodeMapper, QuestionCode>
    implements QuestionCodeService {

}




