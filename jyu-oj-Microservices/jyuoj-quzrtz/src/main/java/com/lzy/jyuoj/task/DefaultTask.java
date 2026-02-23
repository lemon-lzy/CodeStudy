package sspu.zzx.sspuoj.task;

import lombok.extern.slf4j.Slf4j;
import sspu.zzx.sspuoj.constant.CommonConstant;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/11/16 1:49
 */
@Slf4j
public class DefaultTask
{
    public static void timeTell()
    {
        String now = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        log.info("(*＞◡❛)，当前时间为: " + now + "；当前每隔[" + 1.0 * CommonConstant.QUARTZ_TIME_PERIOD / (60 * 60) + "h]报告一次~");
    }
}