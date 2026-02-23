package sspu.zzx.sspuoj.service.impl.sys;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import sspu.zzx.sspuoj.aop.annotation.OpLog;
import sspu.zzx.sspuoj.mapper.OperationLogMapper;
import sspu.zzx.sspuoj.model.entity.OperationLog;
import sspu.zzx.sspuoj.model.enums.user.UserInfoEnum;
import sspu.zzx.sspuoj.utils.NetUtils;
import sun.nio.ch.Net;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Map;


/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/7/20 16:01
 */
@Aspect
@Component
public class SysLogServiceImpl
{
    @Resource
    OperationLogMapper operationLogMapper;

    //定义切点 @Pointcut
    //在注解的位置切入代码
    @Pointcut("@annotation(sspu.zzx.sspuoj.aop.annotation.OpLog)")
    public void logPointCut()
    {
    }

    //切面 配置通知
    @AfterReturning(value = "logPointCut()", returning = "returnValue")
    public void saveSysLog(JoinPoint joinPoint, Object returnValue)
    {
        //System.out.println("在切面保存日志");
        //保存日志
        OperationLog operationLog = new OperationLog();

        //从切面织入点处通过反射机制获取织入点处的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获取切入点所在的方法
        Method method = signature.getMethod();

        /*获取操作名称*/
        //获取操作名称
        OpLog myLog = method.getAnnotation(OpLog.class);
        if (myLog != null)
        {
            String value = myLog.value();
            operationLog.setOperationName(value.split(":")[0]);//保存获取的操作
            /*获取操作的表名*/
            operationLog.setTName(value.split(":")[1]);
        }

        /*获取操作方法名*/
        //获取请求的类名
        String className = joinPoint.getTarget().getClass().getName();
        //获取请求的方法名
        String methodName = method.getName();
        operationLog.setOperationMethod(className + "." + methodName);

        /*获取操作者请求相关信息*/
        Map<String, String> userAgentInfo = NetUtils.getUserAgentInfo(false);
        operationLog.setOperatorIp(userAgentInfo.get("operatorIp"));
        operationLog.setOperatorBrowser(userAgentInfo.get("operatorBrowser"));
        operationLog.setOperatorClient(userAgentInfo.get("operatorClient"));
        operationLog.setOperatorOs(userAgentInfo.get("operatorOs"));

        /*获取操作者id*/
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId != null)
        {
            operationLog.setOperatorId(Long.parseLong(StpUtil.getLoginIdDefaultNull().toString()));
            operationLog.setOperatorToken(StpUtil.getTokenValue());
        }
        /*获取操作的参数*/
        //请求的参数
        Object[] args = joinPoint.getArgs();
        //将参数所在的数组转换成json
        String params = JSONUtil.toJsonStr(args);
        operationLog.setOperationParams(params);

        /*获取操作结果*/
        operationLog.setOperationResult(JSONUtil.toJsonStr(returnValue));

        // 保存日志
        operationLogMapper.insert(operationLog);
    }
}
