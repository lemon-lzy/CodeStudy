package sspu.zzx.sspuoj.utils;

import cn.hutool.core.map.MapUtil;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 网络工具类
 *
 * @author ZZX
 * @from SSPU
 */
@Slf4j
public class NetUtils
{

    public static HttpServletRequest getRequest()
    {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 获得请求方信息
     *
     * @return
     */
    public static Map<String, String> getUserAgentInfo(boolean isPrint)
    {
        HttpServletRequest request = getRequest();
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("user-agent"));
        String clientType = userAgent.getOperatingSystem().getDeviceType().toString();
        String os = userAgent.getOperatingSystem().getName();
        String ip = getIpAddress(request);
        String browser = userAgent.getBrowser().toString();
        if (isPrint)
        {
            log.info("clientType = " + clientType);   //客户端类型  手机、电脑、平板
            log.info("os = " + os);    //操作系统类型
            log.info("ip = " + ip);    //请求ip
            log.info("browser = " + browser);   // 浏览器类型
        }
        Map<String, String> userAgentMap = MapUtil.<String, String>builder().put("operatorClient", clientType).put("operatorOs", os).put("operatorIp", ip).put("operatorBrowser", browser).build();

        return userAgentMap;
    }

    /**
     * 获取客户端 IP 地址
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request)
    {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1"))
            {
                // 根据网卡取本机配置的 IP
                InetAddress inet = null;
                try
                {
                    inet = InetAddress.getLocalHost();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                if (inet != null)
                {
                    ip = inet.getHostAddress();
                }
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15)
        {
            if (ip.indexOf(",") > 0)
            {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        if (ip == null)
        {
            return "127.0.0.1";
        }
        return ip;
    }

    /**
     * 获取客户端 IP 地址
     *
     * @return
     */
    public static String getIpAddress()
    {
        HttpServletRequest request = getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
        {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1"))
            {
                // 根据网卡取本机配置的 IP
                InetAddress inet = null;
                try
                {
                    inet = InetAddress.getLocalHost();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                if (inet != null)
                {
                    ip = inet.getHostAddress();
                }
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15)
        {
            if (ip.indexOf(",") > 0)
            {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        if (ip == null)
        {
            return "127.0.0.1";
        }
        return ip;
    }

}
