package sspu.zzx.sspuoj.utils;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/7/8 21:53
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Component
public class TokenUtils
{
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void saveToken(String userId, String token, Integer timeOut)
    {
        // 将 token 保存到 Redis 中
        redisTemplate.opsForValue().set(userId, token, timeOut, TimeUnit.SECONDS);
    }

    public String getToken(String userId)
    {
        // 从 Redis 中获取 token
        return redisTemplate.opsForValue().get(userId);
    }

    public void removeToken(String userId)
    {
        // 从 Redis 中移除 token
        redisTemplate.delete(userId);
    }

    public Long countKeysWithPrefix(String prefix)
    {
        // 获取指定前缀的key个数
        long count = 0;
        String pattern = prefix + "*";
        ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
        Cursor<byte[]> cursor = redisTemplate.executeWithStickyConnection((RedisCallback<Cursor<byte[]>>) connection -> connection.scan(options));
        while (cursor.hasNext())
        {
            cursor.next();
            count++;
        }
        return count;
    }

    public Map<String, String> getAllKeyValues()
    {
        // 获取所有的key
        Set<String> keys = redisTemplate.keys("*");

        // 创建一个Map来存储所有的key-value对
        Map<String, String> keyValueMap = new HashMap<>();

        // 遍历所有的key，并获取对应的value
        for (String key : keys)
        {
            String value = redisTemplate.opsForValue().get(key);
            keyValueMap.put(key, value);
        }

        return keyValueMap;
    }


}
