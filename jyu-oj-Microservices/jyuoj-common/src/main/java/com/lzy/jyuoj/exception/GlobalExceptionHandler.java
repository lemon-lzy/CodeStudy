package sspu.zzx.sspuoj.exception;

import cn.dev33.satoken.exception.DisableServiceException;
import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sspu.zzx.sspuoj.common.BaseResponse;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.common.ResultUtils;

/**
 * 全局异常处理器
 *
 * @author ZZX
 * @from SSPU
 * @date 2023/11/15
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
{

    /**
     * 捕捉系统业务异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<String> businessExceptionHandler(BusinessException e)
    {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 捕捉运行时异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<String> runtimeExceptionHandler(RuntimeException e)
    {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.FORBIDDEN_ERROR.getValue());
    }


    /**
     * Sa-Token全局异常拦截（拦截项目中的NotLoginException异常）
     *
     * @param nle
     * @return
     * @throws Exception
     */
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<String> handlerNotLoginException(NotLoginException nle) throws Exception
    {
        // 打印堆栈，以供调试
        nle.printStackTrace();

        // 判断场景值，定制化异常信息
        String message = "";
        if (nle.getType().equals(NotLoginException.NOT_TOKEN))
        {
            message = "未能读取到有效 token";
        }
        else if (nle.getType().equals(NotLoginException.INVALID_TOKEN))
        {
            message = "token 无效";
        }
        else if (nle.getType().equals(NotLoginException.TOKEN_TIMEOUT))
        {
            message = "token 已过期";
        }
        else if (nle.getType().equals(NotLoginException.BE_REPLACED))
        {
            message = "token 已被顶下线";
        }
        else if (nle.getType().equals(NotLoginException.KICK_OUT))
        {
            message = "token 已被踢下线";
        }
        else if (nle.getType().equals(NotLoginException.TOKEN_FREEZE))
        {
            message = "token 已被冻结";
        }
        else if (nle.getType().equals(NotLoginException.NO_PREFIX))
        {
            message = "未按照指定前缀提交 token";
        }
        else
        {
            message = "当前会话未登录";
        }

        // 返回给前端
        return ResultUtils.error(ErrorCode.OPERATION_ERROR, message);
    }

    /**
     * 捕捉被封号异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(DisableServiceException.class)
    public BaseResponse<String> runtimeExceptionHandler(DisableServiceException e)
    {
        log.error("DisableServiceException", e);
        return ResultUtils.error(ErrorCode.FORBIDDEN_ERROR, "您已被封号！请联系管理员解封");
    }

    /**
     * 捕捉其余所有异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public BaseResponse<String> handlerException(Exception e)
    {
        e.printStackTrace();
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }

}
