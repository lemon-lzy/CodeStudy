package sspu.zzx.sspuoj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import sspu.zzx.sspuoj.constant.CommonConstant;

/**
 * 主类（项目启动入口）
 *
 * @author ZZX
 * @from SSPU
 */
// todo 如需开启 Redis，须移除 exclude 中的内容
@SpringBootApplication
@MapperScan("sspu.zzx.sspuoj.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MainApplication implements CommandLineRunner
{

    public static void main(String[] args)
    {
        SpringApplication.run(MainApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {
        QuartzExecute.doQuartzTask(CommonConstant.QUARTZ_TIME_PERIOD);
    }
}
