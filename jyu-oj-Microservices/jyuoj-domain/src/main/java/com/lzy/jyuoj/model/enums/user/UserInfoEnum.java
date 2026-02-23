package sspu.zzx.sspuoj.model.enums.user;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色枚举
 *
 * @author ZZX
 * @from SSPU
 */
public enum UserInfoEnum
{

    USER_DEFAULT_AVATAR("用户默认头像链接", "http://minio.crzzx.xyz:9000/sspuoj/user-default.jpg"),
    USER_DEFAULT_PROFILE("用户默认简介", "简介还为空~"),
    USER_DEFAULT_TYPE("用户默认类型", UserRoleEnum.USER.getValue());

    private final String text;

    private final String value;

    UserInfoEnum(String text, String value)
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
    public static UserInfoEnum getEnumByValue(String value)
    {
        if (ObjectUtils.isEmpty(value))
        {
            return null;
        }
        for (UserInfoEnum anEnum : UserInfoEnum.values())
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
