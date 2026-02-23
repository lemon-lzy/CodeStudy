package sspu.zzx.sspuoj.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题解类型消息枚举
 *
 * @author ZZX
 * @from SSPU
 */
public enum SolutionTypeEnum
{

    Q_ANSWER("题解", "answer"),
    Q_NOTICE("公告", "notice"),
    Q_DAILY("日报", "daily");

    private final String text;

    private final String value;

    SolutionTypeEnum(String text, String value)
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
    public static SolutionTypeEnum getEnumByValue(String value)
    {
        if (ObjectUtils.isEmpty(value))
        {
            return null;
        }
        for (SolutionTypeEnum anEnum : SolutionTypeEnum.values())
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
