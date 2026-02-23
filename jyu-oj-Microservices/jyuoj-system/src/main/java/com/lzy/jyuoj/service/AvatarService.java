package sspu.zzx.sspuoj.service;

import java.io.IOException;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/11/18 20:39
 */
public interface AvatarService
{
    String getDefaultAvatarByFullName(String fullName) throws IOException;
}
