package sspu.zzx.sspuoj.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 状态枚举
 *
 * @author ZZX
 * @from SSPU
 */
public enum StateEnum
{

    HANDLED("已处理", "已处理"), UN_HANDLED("未处理", "未处理"), ORGAN_IN("组织内成员", "在职"), ORGAN_ING("申请加入组织", "申请中"), ORGAN_BLACK("不被允许加入组织", "被拉黑");

    private final String text;

    private final String value;

    StateEnum(String text, String value)
    {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues()
    {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static StateEnum getEnumByValue(String value)
    {
        if (ObjectUtils.isEmpty(value))
        {
            return null;
        }
        for (StateEnum anEnum : StateEnum.values())
        {
            if (anEnum.value.equals(value))
            {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue()
    {
        return value;
    }

    public String getText()
    {
        return text;
    }
}
