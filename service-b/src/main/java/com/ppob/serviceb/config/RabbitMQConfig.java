package com.ppob.serviceb.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${ppob.rabbitmq.exchange}")                          private String exchange;

    @Value("${ppob.rabbitmq.queue.auth-request}")               private String authRequestQueue;
    @Value("${ppob.rabbitmq.queue.payment-request}")            private String paymentRequestQueue;
    @Value("${ppob.rabbitmq.queue.profile-request}")            private String profileRequestQueue;
    @Value("${ppob.rabbitmq.queue.logout-request}")             private String logoutRequestQueue;

    @Value("${ppob.rabbitmq.reply-queue.auth-reply}")           private String authReplyQueue;
    @Value("${ppob.rabbitmq.reply-queue.payment-reply}")        private String paymentReplyQueue;
    @Value("${ppob.rabbitmq.reply-queue.profile-reply}")        private String profileReplyQueue;
    @Value("${ppob.rabbitmq.reply-queue.logout-reply}")         private String logoutReplyQueue;

    @Value("${ppob.rabbitmq.routing-key.auth-request}")         private String authRequestRoutingKey;
    @Value("${ppob.rabbitmq.routing-key.payment-request}")      private String paymentRequestRoutingKey;
    @Value("${ppob.rabbitmq.routing-key.profile-request}")      private String profileRequestRoutingKey;
    @Value("${ppob.rabbitmq.routing-key.logout-request}")       private String logoutRequestRoutingKey;

    @Value("${ppob.rabbitmq.routing-key.auth-reply}")           private String authReplyRoutingKey;
    @Value("${ppob.rabbitmq.routing-key.payment-reply}")        private String paymentReplyRoutingKey;
    @Value("${ppob.rabbitmq.routing-key.profile-reply}")        private String profileReplyRoutingKey;
    @Value("${ppob.rabbitmq.routing-key.logout-reply}")         private String logoutReplyRoutingKey;

    @Bean public TopicExchange ppobExchange() { return new TopicExchange(exchange, true, false); }

    @Bean public Queue authRequestQueue()    { return QueueBuilder.durable(authRequestQueue).build(); }
    @Bean public Queue paymentRequestQueue() { return QueueBuilder.durable(paymentRequestQueue).build(); }
    @Bean public Queue profileRequestQueue() { return QueueBuilder.durable(profileRequestQueue).build(); }
    @Bean public Queue logoutRequestQueue()  { return QueueBuilder.durable(logoutRequestQueue).build(); }

    @Bean public Queue authReplyQueue()    { return QueueBuilder.durable(authReplyQueue).build(); }
    @Bean public Queue paymentReplyQueue() { return QueueBuilder.durable(paymentReplyQueue).build(); }
    @Bean public Queue profileReplyQueue() { return QueueBuilder.durable(profileReplyQueue).build(); }
    @Bean public Queue logoutReplyQueue()  { return QueueBuilder.durable(logoutReplyQueue).build(); }

    @Bean public Binding authRequestBinding()    { return BindingBuilder.bind(authRequestQueue()).to(ppobExchange()).with(authRequestRoutingKey); }
    @Bean public Binding paymentRequestBinding() { return BindingBuilder.bind(paymentRequestQueue()).to(ppobExchange()).with(paymentRequestRoutingKey); }
    @Bean public Binding profileRequestBinding() { return BindingBuilder.bind(profileRequestQueue()).to(ppobExchange()).with(profileRequestRoutingKey); }
    @Bean public Binding logoutRequestBinding()  { return BindingBuilder.bind(logoutRequestQueue()).to(ppobExchange()).with(logoutRequestRoutingKey); }

    @Bean public Binding authReplyBinding()    { return BindingBuilder.bind(authReplyQueue()).to(ppobExchange()).with(authReplyRoutingKey); }
    @Bean public Binding paymentReplyBinding() { return BindingBuilder.bind(paymentReplyQueue()).to(ppobExchange()).with(paymentReplyRoutingKey); }
    @Bean public Binding profileReplyBinding() { return BindingBuilder.bind(profileReplyQueue()).to(ppobExchange()).with(profileReplyRoutingKey); }
    @Bean public Binding logoutReplyBinding()  { return BindingBuilder.bind(logoutReplyQueue()).to(ppobExchange()).with(logoutReplyRoutingKey); }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}