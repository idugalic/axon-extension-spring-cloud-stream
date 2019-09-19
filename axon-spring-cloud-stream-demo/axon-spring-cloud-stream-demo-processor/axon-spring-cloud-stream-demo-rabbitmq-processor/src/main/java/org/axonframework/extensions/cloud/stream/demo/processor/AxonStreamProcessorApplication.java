package org.axonframework.extensions.cloud.stream.demo.processor;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.extensions.stream.converter.DefaultSpringMessageEventMessageConverter;
import org.axonframework.extensions.stream.converter.SpringMessageEventMessageConverter;
import org.axonframework.extensions.stream.processor.MessageProducingHandler;
import org.axonframework.messaging.interceptors.LoggingInterceptor;
import org.axonframework.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

@SpringBootApplication
@EnableBinding(Processor.class)
public class AxonStreamProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AxonStreamProcessorApplication.class, args);
    }

    @Bean
    public SpringMessageEventMessageConverter springMessageEventMessageConverter(Serializer serializer) {
        return new DefaultSpringMessageEventMessageConverter(serializer);
    }

    @Bean
    @Qualifier("amqpMessageSource")
    MessageProducingHandler amqpMessageSource(EventBus eventBus,
                                              SpringMessageEventMessageConverter springMessageEventMessageConverter) {

        return new MessageProducingHandler(eventBus, springMessageEventMessageConverter);
    }

    @Bean
    public IntegrationFlow flow(MessageProducingHandler amqpMessageSource) {
        return IntegrationFlows.from(Processor.INPUT)
                               .handle(amqpMessageSource)
                               .channel(Processor.OUTPUT)
                               .get();
    }

    @Autowired
    public void configure(EventProcessingConfigurer config) {
        config.registerDefaultHandlerInterceptor((configuration, s) -> new LoggingInterceptor(s));
    }
}

