package com.example.wait4eat.global.config;

import com.example.wait4eat.infra.redis.subscriber.RedisSubscriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisSubscriber redisSubscriber;
    private final ObjectMapper objectMapper;

    @Value("${redis.notification-topic}")
    private String notificationTopic;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 구독 채널 등록
        container.addMessageListener(redisSubscriber, new PatternTopic(notificationTopic));
        return container;
    }

    @Bean
    public ChannelTopic notificationChannelTopic() {
        return new ChannelTopic(notificationTopic);
    }
}
