package sspu.zzx.sspuoj.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Date;
import java.util.Random;

/**
 * 邮件工具类
 *
 * @version 1.0
 * @Author ZZX
 * @Date 2023/7/17 14:06
 */
@Service
@Slf4j
public class MailUtils
{
    /**
     * 注入邮件工具类
     */
    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.username}")
    private String sendMailer;

    /**
     * 检测邮件信息类
     *
     * @param to
     * @param subject
     * @param text
     */
    private void checkMail(String to, String subject, String text)
    {
        if (StringUtils.isEmpty(to))
        {
            throw new RuntimeException("邮件收信人不能为空");
        }
        if (StringUtils.isEmpty(subject))
        {
            throw new RuntimeException("邮件主题不能为空");
        }
        if (StringUtils.isEmpty(text))
        {
            throw new RuntimeException("邮件内容不能为空");
        }
    }

    /**
     * 发送纯文本邮件
     *
     * @param to
     * @param subject
     * @param text
     */
    public boolean sendTextMailMessage(String to, String subject, String text)
    {

        try
        {
            //true 代表支持复杂的类型
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);
            //邮件发信人
            mimeMessageHelper.setFrom(sendMailer);
            //邮件收信人  1或多个
            mimeMessageHelper.setTo(to.split(","));
            //邮件主题
            mimeMessageHelper.setSubject(subject);
            //邮件内容
            mimeMessageHelper.setText(text);
            //邮件发送时间
            mimeMessageHelper.setSentDate(new Date());

            //发送邮件
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            log.info("发送邮件成功：" + sendMailer + "->" + to);
            return true;

        } catch (MessagingException e)
        {
            e.printStackTrace();
            log.info("发送邮件失败：" + e.getMessage());
            return false;
        }
    }

    public String verifyCode(int n)
    {
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < n; i++)
        {
            int ran1 = r.nextInt(10);
            sb.append(String.valueOf(ran1));
        }
        return sb.toString();
    }
}


