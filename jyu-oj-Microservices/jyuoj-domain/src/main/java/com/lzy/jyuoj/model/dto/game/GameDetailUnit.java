package sspu.zzx.sspuoj.model.dto.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2024/1/10 10:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameDetailUnit implements Serializable
{
    /**
     * 单题id
     */
    private Long id;

    /**
     * 单题名称
     */
    private String name;

    /**
     * 单题得分
     */
    private Integer score;

    /**
     * 单题耗时
     */
    private Integer timeCost;

    /**
     * 单题耗内存
     */
    private Integer memoryCost;

    /**
     * 比较是否比另一个好
     *
     * @param other
     * @return
     */
    public boolean isBetter(GameDetailUnit other)
    {
        // 首先比较分数是否更大
        if (this.score > other.getScore())
        {
            return true;
        }
        if (this.score < other.getScore())
        {
            return false;
        }
        // 其次比较耗时是否更少
        if (this.timeCost < other.getTimeCost())
        {
            return true;
        }
        if (this.timeCost > other.getTimeCost())
        {
            return false;
        }
        // 最后比较耗费空间是否更少
        if (this.memoryCost < other.getMemoryCost())
        {
            return true;
        }
        return false;
    }

    private static final long serialVersionUID = 1L;
}
