package sspu.zzx.sspuoj.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;
import sspu.zzx.sspuoj.aichat.domain.RoleContent;
import sspu.zzx.sspuoj.aichat.domain.request.Request;
import sspu.zzx.sspuoj.aichat.domain.request.header.Header;
import sspu.zzx.sspuoj.aichat.domain.request.parameter.Chat;
import sspu.zzx.sspuoj.aichat.domain.request.parameter.Parameter;
import sspu.zzx.sspuoj.aichat.domain.request.payload.Message;
import sspu.zzx.sspuoj.aichat.domain.request.payload.Payload;
import sspu.zzx.sspuoj.aichat.domain.response.Result;
import sspu.zzx.sspuoj.aichat.domain.response.payload.Text;
import sspu.zzx.sspuoj.common.BaseResponse;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.common.ResultUtils;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.utils.TokenUtils;
import sspu.zzx.sspuoj.utils.encrypt.RSAUtil;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static sspu.zzx.sspuoj.constant.CommonConstant.*;


/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/11/30 01:15
 */
@RestController
@RequestMapping("/ai/chat")
@Slf4j
public class AiChatController
{
    /**
     * 请求地址
     */
    @Value("${xunfei.host-url}")
    private static String HOST_URL = "https://spark-api.xf-yun.com/v1.1/chat";

    /**
     * v1版本
     */
    private static final String DOMAIN = "general";

    /**
     * todo 更换为自己的appid、secret和key
     * APPID
     */
    private static final String APPID = "0913e49d";

    /**
     * APISecret
     */
    private static final String API_SECRET = "MWUzNjc1YjI2NDYwYTU0NDgwZDk4NTFh";

    /**
     * APIKey
     */
    private static final String API_KEY = "7f06437ad56e75b12d1ebd53396ebe6a";

    /**
     * user表示是用户的问题
     */
    private static final String ROLE_USER = "user";

    /**
     * assistant表示AI的回复
     */
    private static final String ROLE_ASSISTANT = "assistant";


    @Autowired
    private TokenUtils tokenUtils;

    /**
     * URL鉴权
     *
     * @return 请求url
     * @throws MalformedURLException 异常
     */
    private static String getAuthUrl() throws MalformedURLException
    {
        String date = DateUtil.format(new Date(), DatePattern.HTTP_DATETIME_FORMAT);
        URL url = new URL(HOST_URL);
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";

        HMac hMac = SecureUtil.hmacSha256(API_SECRET.getBytes(StandardCharsets.UTF_8));
        byte[] digest = hMac.digest(preStr);
        String signature = Base64.encode(digest);
        String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                API_KEY, "hmac-sha256", "host date request-line", signature);
        String authorization = Base64.encode(authorizationOrigin);
        return UriComponentsBuilder.fromUriString(HOST_URL)
                .queryParam("authorization", authorization)
                .queryParam("date", date)
                .queryParam("host", url.getHost()).toUriString();
    }


    /**
     * 生成请求参数
     *
     * @param content     对话内容
     * @param historyList 历史记录
     * @return 请求参数
     */
    public static String createReqParams(String content, List<RoleContent> historyList)
    {
        // 组装接口请求参数
        Header header = new Header();
        header.setAppId(APPID);

        Chat chat = new Chat();
        chat.setDomain(DOMAIN);
        Parameter parameter = new Parameter();
        parameter.setChat(chat);

        Message message = new Message();

        if (historyList.isEmpty())
        {
            sspu.zzx.sspuoj.aichat.domain.request.payload.Text text = new sspu.zzx.sspuoj.aichat.domain.request.payload.Text();
            text.setRole(ROLE_USER);
            text.setContent(content);
            message.setText(Collections.singletonList(text));
        } else
        {
            List<sspu.zzx.sspuoj.aichat.domain.request.payload.Text> textList = historyList.stream()
                    .map(item ->
                    {
                        sspu.zzx.sspuoj.aichat.domain.request.payload.Text text = new sspu.zzx.sspuoj.aichat.domain.request.payload.Text();
                        text.setContent(item.getContent());
                        text.setRole(item.getRole());
                        return text;
                    })
                    .collect(Collectors.toList());
            message.setText(textList);
        }

        Payload payload = new Payload();
        payload.setMessage(message);

        Request request = new Request();
        request.setHeader(header);
        request.setParameter(parameter);
        request.setPayload(payload);
        //
        return JSONObject.toJSONString(request);
    }


    /**
     * 建立sse连接，以便逐字返回信息
     *
     * @param loginToken
     * @return
     */
    @SaIgnore
    @CrossOrigin
    @GetMapping("/reply")
    public SseEmitter streamSseReply(@RequestParam("loginToken") String loginToken) throws Exception
    {
        //私钥解密
        PrivateKey privateKey = RSAUtil.getPrivateKey(RSA_PRIVATE_KEY);
        String newLoginToken = RSAUtil.decryptString(privateKey, loginToken);
        // 设置sse连接的超时时间为600000毫秒（6000秒 = 100分钟）
        SseEmitter emitter = new SseEmitter(100 * 60 * 1000L);
        log.info("建立AI智能对话-sse通信连接成功! 此时 loginToken = {}", newLoginToken);
        // 获得校验后的url和参数
        String authUrl = getAuthUrl();
        String historyToken = tokenUtils.getToken(AI_CHAT_HISTORY_PREFIX + newLoginToken);
        JSONArray array = JSONUtil.parseArray(historyToken);
        List<RoleContent> aiHistory = JSONUtil.toList(array, RoleContent.class);
        String content = tokenUtils.getToken(AI_CHAT_PREFIX + newLoginToken);
        String reqParams = createReqParams(content, aiHistory);
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        URI uri = new URI(url);
        StringBuffer sb = new StringBuffer();
        WebSocketClient webSocketClient = new WebSocketClient(uri)
        {
            @Override
            public void onOpen(ServerHandshake serverHandshake)
            {
                send(reqParams);
            }

            @SneakyThrows
            @Override
            public void onMessage(String s)
            {
                // 错误码，0表示正常
                final int successCode = 0;
                // 会话状态，2代表最后一个结果
                final int lastStatus = 2;

                Result result = JSONObject.parseObject(s, Result.class);
                sspu.zzx.sspuoj.aichat.domain.response.header.Header header = result.getHeader();
                if (Objects.equals(successCode, header.getCode()))
                {
                    emitter.send(result);
                    List<Text> text = result.getPayload().getChoices().getText();
                    if (text.size() > 0)
                    {
                        sb.append(text.get(0).getContent());
                    }
                } else
                {
                    log.error("大模型接口响应异常，错误码：{}，sid：{}", header.getCode(), header.getSid());
                }

                // 如果是最后的结果，关闭ws连接
                if (Objects.equals(lastStatus, header.getStatus()))
                {
                    tokenUtils.removeToken(AI_CHAT_PREFIX + newLoginToken);
                    tokenUtils.removeToken(AI_CHAT_HISTORY_PREFIX + newLoginToken);
                    this.close();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b)
            {
                log.info("WebSocket连接已关闭，原因：{}，状态码：{}，是否远程关闭：{}", i, s, b);
            }

            @Override
            public void onError(Exception e)
            {
                log.error("大模型接口调用发生异常，异常原因:{},异常位置:{}", e.getMessage(), e.getStackTrace()[0]);
            }
        };
        webSocketClient.connect();
        return emitter;
    }

    /**
     * 预存提问信息和历史记录
     *
     * @param askContent
     * @param historyList
     * @return
     */
    @PostMapping("/ask")
    public BaseResponse<Boolean> saveAskContent(@RequestParam("askContent") String askContent, @RequestParam("historyList") String historyList)
    {
        // 长度校验
        if (StringUtils.isBlank(askContent) || askContent.length() > 1000)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提问内容不能为空，且长度不能超过1000！");
        }
        // 保存askContent到redis
        String tokenValue = StpUtil.getTokenValue();
        if (StringUtils.isNotBlank(tokenValue))
        {
            tokenUtils.saveToken(AI_CHAT_PREFIX + tokenValue, askContent, AI_TIME_OUT);
            if (historyList != null)
            {
                tokenUtils.saveToken(AI_CHAT_HISTORY_PREFIX + tokenValue, historyList, AI_TIME_OUT);
            }
        }
        return ResultUtils.success(true);
    }

}
