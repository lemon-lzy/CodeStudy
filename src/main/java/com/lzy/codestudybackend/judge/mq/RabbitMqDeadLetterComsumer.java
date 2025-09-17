package com.lzy.codestudybackend.judge.mq;

import cn.hutool.json.JSONUtil;
import com.lzy.codestudybackend.common.ErrorCode;
import com.lzy.codestudybackend.common.MqConstant;
import com.lzy.codestudybackend.exception.BusinessException;
import com.lzy.codestudybackend.model.dto.questionSubmit.QuestionSubmitMqAddRequest;
import com.lzy.codestudybackend.model.entity.question.QuestionSubmit;
import com.lzy.codestudybackend.model.enums.QuestionSubmitStatusEnum;
import com.lzy.codestudybackend.service.question.QuestionSubmitService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 判题死信队列
 *
 * @author Shier
 */
@Component
@Slf4j

public class RabbitMqDeadLetterComsumer {

    @Resource
    private QuestionSubmitService questionSubmitService;


    /**
     * 监听死信队列
     *
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @SneakyThrows
    @RabbitListener(queues = {MqConstant.DEAD_LETTER_QUEUE}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        // 接收到失败的信息
        log.info("死信队列接受到的消息：{}", message);
        if (StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息为空");
        }
        QuestionSubmitMqAddRequest questionSubmitMqAddRequest= JSONUtil.toBean(message, QuestionSubmitMqAddRequest.class);
        long questionSubmitId =questionSubmitMqAddRequest.getQuestionSubmitId();
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交的题目信息不存在");
        }
        // 把提交题目标为失败
        questionSubmit.setSubmitState(QuestionSubmitStatusEnum.FAILED.getValue());
        boolean update = questionSubmitService.updateById(questionSubmit);
        if (!update) {
            log.info("处理死信队列消息失败,对应提交的题目id为:{}", questionSubmit.getId());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "处理死信队列消息失败");
        }
        // 确认消息
        channel.basicAck(deliveryTag, false);
    }
}




