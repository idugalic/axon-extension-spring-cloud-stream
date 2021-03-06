package org.axonframework.extensions.cloud.stream.demo.processor;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy;
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
public class AxonStreamKafkaProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AxonStreamKafkaProcessorApplication.class, args);
    }

    @Bean
    public SpringMessageEventMessageConverter springMessageEventMessageConverter(Serializer serializer) {
        return new DefaultSpringMessageEventMessageConverter(serializer, SequentialPerAggregatePolicy.instance());
    }

    @Bean
    @Qualifier("kafkaMessageSource")
    MessageProducingHandler kafkaMessageSource(EventBus eventBus,
                                               SpringMessageEventMessageConverter springMessageEventMessageConverter) {

        return new MessageProducingHandler(eventBus, springMessageEventMessageConverter);
    }


    @Bean
    public IntegrationFlow flow(MessageProducingHandler kafkaMessageSource) {
        return IntegrationFlows.from(Processor.INPUT)
                               .handle(kafkaMessageSource)
                               .channel(Processor.OUTPUT)
                               .get();
    }

    @Autowired
    public void configure(EventProcessingConfigurer config) {
        config.registerDefaultHandlerInterceptor((configuration, s) -> new LoggingInterceptor(s));
    }
}

