package com.lzy.codestudybackend.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
@Data
public class UserRecommendVO implements Serializable {

    // /**
    //  * id
    //  */
    // private Long id;

    /**
     * 被推荐的用户id
     */
    private Long recommendUserId;

    /**
     * 推荐的用户
     */
    private UserVO recommendUser;

    /**
     * 推荐分数
     */
    private Float score;

    /**
     * 推荐原因
     */
    private String reason;

    /**
     * 共同标签
     */
    private List<String> tags;

    /**
     * 推荐状态
     */
    private Integer status;


    @Serial
    private static final long serialVersionUID = 7373468974376045630L;
}
