package sspu.zzx.sspuoj.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/12/21 22:46
 */
@Component
@Slf4j
public class RetryCallbackHandler
{

    @Recover
    public void fallback(Exception e)
    {
        // 降级处理逻辑
        log.info("重试达到最大次数，请求信息：{}", e.getMessage());
        e.printStackTrace(); // 打印异常堆栈信息
    }

}