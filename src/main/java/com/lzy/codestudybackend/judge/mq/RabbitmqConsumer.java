package com.lzy.codestudybackend.judge.mq;

import cn.hutool.json.JSONUtil;
import com.lzy.codestudybackend.common.MqConstant;
import com.lzy.codestudybackend.exception.BusinessException;
import com.lzy.codestudybackend.judge.JudgeManager;
import com.lzy.codestudybackend.judge.JudgeService;
import com.lzy.codestudybackend.model.dto.questionSubmit.QuestionSubmitAddRequest;
import com.lzy.codestudybackend.model.dto.questionSubmit.QuestionSubmitMqAddRequest;
import com.lzy.codestudybackend.model.entity.question.QuestionSubmit;
import com.lzy.codestudybackend.model.enums.QuestionSubmitStatusEnum;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author lzy
 **/
@Component
@Slf4j
public class RabbitmqConsumer {

    @Resource
    private  JudgeService judgeService;

    // 指定程序监听的消息队列和确认机制
    @RabbitListener(queues = {MqConstant.NORMAL_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("接收到的消息为 = {}", message);
        QuestionSubmitMqAddRequest questionSubmitMqAddRequest= JSONUtil.toBean(message, QuestionSubmitMqAddRequest.class);
        long questionSubmitId =questionSubmitMqAddRequest.getQuestionSubmitId();
        QuestionSubmitAddRequest questionSubmitAddRequest=new QuestionSubmitAddRequest();
        BeanUtils.copyProperties(questionSubmitMqAddRequest,questionSubmitAddRequest);
        try {
            QuestionSubmit questionSubmit = judgeService.doJudge(questionSubmitId,questionSubmitAddRequest);
            if (questionSubmit.getSubmitState()!= QuestionSubmitStatusEnum.WAITING.getValue()) {
                System.out.println("成功处理消息");
                channel.basicAck(deliveryTag, false); // 成功处理消息，确认
            } else {
                System.out.println("状态还是等待，下放到死信队列（如果doJudge返回false，表示需要重新处理该消息）");
                channel.basicNack(deliveryTag, false, false); // 如果doJudge返回false，表示需要重新处理该消息
            }
        } catch (BusinessException e) {
            if (e.getMessage().equals("题目正在判题中")){
                System.out.println("异常信息是已经有在判题的消息了，所以不用再执行直接确认");
                channel.basicAck(deliveryTag, false); // 成功处理消息，确认
            }else {
                System.out.println("其他的异常，比如更新错误，下放到死信");
                channel.basicNack(deliveryTag, false, false);
                throw new RuntimeException(e);
            }
        } catch (Throwable t) { // 捕获所有其他类型的异常
            System.out.println("其他异常，下放到死信");
            channel.basicNack(deliveryTag, false, false); // 发生其他异常，重新入队
            throw t; // 重新抛出异常，让Lombok的@SneakyThrows处理
        }
    }

}

