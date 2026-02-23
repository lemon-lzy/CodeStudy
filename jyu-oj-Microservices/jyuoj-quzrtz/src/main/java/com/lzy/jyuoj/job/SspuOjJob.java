package sspu.zzx.sspuoj.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import sspu.zzx.sspuoj.task.DefaultTask;


/**
 * 新建 Quartz的Job 任务
 */
@Slf4j
public class SspuOjJob implements Job
{

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        DefaultTask.timeTell();
    }
}
