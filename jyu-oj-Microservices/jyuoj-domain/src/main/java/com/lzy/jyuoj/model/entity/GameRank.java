package sspu.zzx.sspuoj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ZZX
 * @TableName game_rank
 */
@TableName(value = "game_rank")
@Data
public class GameRank implements Serializable
{
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 竞赛id
     */
    private Long gameId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 提交总空间消耗
     */
    private Integer totalMemory;

    /**
     * 提交总耗时
     */
    private Integer totalTime;

    /**
     * 竞赛总得分
     */
    private Integer totalScore;

    /**
     * 竞赛详情
     */
    private String gameDetail;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}