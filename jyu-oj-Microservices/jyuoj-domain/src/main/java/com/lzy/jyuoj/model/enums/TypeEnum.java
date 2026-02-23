package sspu.zzx.sspuoj.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类型枚举
 *
 * @author ZZX
 * @from SSPU
 */
public enum TypeEnum
{

    /**
     * 编程题类型
     */
    CODE_QUESTION("编程题", "编程题"),

    /**
     * 常规题类型
     */
    ROUTINE_QUESTION("常规题", "常规题"),

    /**
     * 基础型竞赛
     */
    GAME_BASIC("基础型竞赛", "基础型"),

    /**
     * 提高型竞赛
     */
    GAME_UPPER("提高型竞赛", "提高型"),

    /**
     * 综合型竞赛
     */
    GAME_SYNTHESIS("综合型竞赛", "综合型");

    private final String text;

    private final String value;

    TypeEnum(String text, String value)
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
    public static TypeEnum getEnumByValue(String value)
    {
        if (ObjectUtils.isEmpty(value))
        {
            return null;
        }
        for (TypeEnum anEnum : TypeEnum.values())
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
