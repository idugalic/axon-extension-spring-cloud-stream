package org.axonframework.extensions.cloud.stream.demo.source;

import org.axonframework.eventhandling.EventBus;
import org.axonframework.extensions.stream.converter.DefaultSpringMessageEventMessageConverter;
import org.axonframework.extensions.stream.converter.SpringMessageEventMessageConverter;
import org.axonframework.extensions.stream.inbound.MessageProducer;
import org.axonframework.serialization.Serializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

@SpringBootApplication
@EnableBinding(Source.class)
public class AxonStreamRabbitMQSourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AxonStreamRabbitMQSourceApplication.class, args);
    }

    @Bean
    public SpringMessageEventMessageConverter springMessageEventMessageConverter(Serializer serializer) {
        return new DefaultSpringMessageEventMessageConverter(serializer);
    }

    @Bean
    public MessageProducer messageProducer(EventBus eventBus, SpringMessageEventMessageConverter converter) {
        return new MessageProducer(eventBus, converter);
    }

    @Bean
    public IntegrationFlow flow(MessageProducer messageProducer) {
        return IntegrationFlows.from(messageProducer)
                               .channel(Source.OUTPUT)
                               .get();
    }
}

