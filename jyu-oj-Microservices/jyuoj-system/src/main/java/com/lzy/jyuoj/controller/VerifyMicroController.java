package sspu.zzx.sspuoj.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import sspu.zzx.sspuoj.common.BaseResponse;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.common.ResultUtils;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.utils.MailUtils;
import sspu.zzx.sspuoj.utils.RandomValidateCodeUtils;
import sspu.zzx.sspuoj.utils.TokenUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static sspu.zzx.sspuoj.constant.CommonConstant.EMAIL_CODE_TIMEOUT;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/11/18 2:02
 */
@RestController
@RequestMapping("/verify")
public class VerifyMicroController
{
    @Autowired
    TokenUtils tokenUtils;
    @Autowired
    private MailUtils mailUtils;
    @Value("${spring.application.name}")
    private String text;

    private final static Logger logger = LoggerFactory.getLogger(VerifyMicroController.class);

    /*邮件：MainController*/

    /**
     * 发送邮件
     *
     * @param email
     * @return
     */
    @PostMapping("/mail/send")
    BaseResponse<String> sendEmail(@RequestParam String email)
    {
        // 检查是否已存在该邮箱的验证码，防止重复发送，浪费资源
        String preCode = tokenUtils.getToken(email);
        if (preCode != null)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码尚未过期，请输入已获得的验证码！");
        }
        // 发送邮箱验证码
        String code = mailUtils.verifyCode(6);
        boolean isSuccess = mailUtils.sendTextMailMessage(email, "账号注册或密码找回：" + text, "[" + text + "]用户验证码：" + code + "（5分钟内有效）。为确保信息安全，请勿向他人透露。");
        if (!isSuccess)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "此邮箱不存在，请更换已注册的邮箱！");
        }
        // 保存邮箱验证码
        tokenUtils.saveToken(email, code, EMAIL_CODE_TIMEOUT);
        return ResultUtils.success("获取验证码成功");
    }

    /**
     * 验证邮箱验证码
     *
     * @param email
     * @param verifyCode
     * @return
     */
    @PostMapping("/mail/check")
    BaseResponse<String> checkEmailCode(@RequestParam String email, @RequestParam String verifyCode)
    {
        // 检验验证码是否过期或正确（根据邮箱获取redis中的值）
        String code = tokenUtils.getToken(email);
        if (code == null)
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "验证码已失效，请重新获取！");
        }
        if (!verifyCode.equals(code))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码输入错误，请核对重试！");
        }
        return ResultUtils.success("邮箱验证码验证成功！");
    }

    /*图像验证码：PicVerifyActionController*/

    /**
     * 生成验证码
     */
    @GetMapping("/verify/get")
    public void getVerify(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            //设置相应类型,告诉浏览器输出的内容为图片
            response.setContentType("image/jpeg");

            //设置响应头信息，告诉浏览器不要缓存此内容
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expire", 0);

            RandomValidateCodeUtils randomValidateCode = new RandomValidateCodeUtils();

            String randomCode = randomValidateCode.getRandomCode(request, response);//输出验证码图片方法
            // 保存验证码至redis
            tokenUtils.saveToken("verifyCode", randomCode, 60 * 5);
        }
        catch (Exception e)
        {
            logger.error("获取验证码失败>>>>   ", e);
        }

    }


    /**
     * 校验验证码
     */
    @GetMapping("/verify/check")
    public BaseResponse<String> checkVerify(@RequestParam String verityCode)
    {
        try
        {
            String random = tokenUtils.getToken("verifyCode");

            if (random == null || "".equals(random) || !random.equalsIgnoreCase(verityCode))
            {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码不匹配！请重新输入或点击图片重新获取验证码！");
            }
            else
            {
                return ResultUtils.success("验证成功！");
            }

        }
        catch (Exception e)
        {
            logger.error("验证码校验失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码校验失败！");
        }
    }
}