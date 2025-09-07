package com.lzy.codestudybackend.model.entity.recommend;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "question_recommend")
@Data
public class QuestionRecommend implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long questionId;
    
    private Float score;
    
    private String reason;
    
    private String type;
    
    private Integer status;
    
    private Date createTime;
    
    private Date updateTime;
    
    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}