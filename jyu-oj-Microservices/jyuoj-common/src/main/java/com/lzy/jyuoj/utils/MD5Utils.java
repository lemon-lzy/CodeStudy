package sspu.zzx.sspuoj.utils;

import java.security.MessageDigest;
import java.util.Random;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/7/17 9:37
 */
public class MD5Utils
{
    /**
     * md5 加密
     *
     * @param saltAndPwd
     * @return
     */
    public static String encode(String saltAndPwd)
    {
        try
        {
            return toHex(MessageDigest.getInstance("MD5").digest(saltAndPwd.getBytes("UTF-8"))).toLowerCase();
        }
        catch (Exception e)
        {
            throw new RuntimeException("md5 加密", e);
        }
    }

    /**
     * 十六进制字符
     */
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    /**
     * 转换为十六进制字符串
     *
     * @param bytes
     * @return
     */
    private static String toHex(byte[] bytes)
    {
        StringBuilder str = new StringBuilder(bytes.length * 2);
        final int fifteen = 0x0f;//十六进制中的 15
        for (byte b : bytes)
        {//byte 为 32 位
            str.append(HEX_CHARS[(b >> 4) & fifteen]);//获取第 25 位到第 28 位的二进制数
            str.append(HEX_CHARS[b & fifteen]);//获取第 29 位到第 32 位的二进制数
        }
        return str.toString();
    }

    /**
     * 随机生成10位密码盐
     *
     * @return
     */
    public static String getSalt()
    {
        char[] chars = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" + "1234567890!@#$%^&*()_+").toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++)
        {
            //Random().nextInt()返回值为[0,n)
            char aChar = chars[new Random().nextInt(chars.length)];
            sb.append(aChar);
        }
        return sb.toString();
    }

}
