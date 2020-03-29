package org.axonframework.extensions.cloud.stream.demo.sink;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.extensions.stream.converter.DefaultSpringMessageEventMessageConverter;
import org.axonframework.extensions.stream.converter.SpringMessageEventMessageConverter;
import org.axonframework.extensions.stream.outbound.MessageHandler;
import org.axonframework.extensions.stream.outbound.MultiSubscribableMessageSource;
import org.axonframework.messaging.interceptors.LoggingInterceptor;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import java.util.Arrays;

@SpringBootApplication
@EnableBinding(Sink.class)
public class AxonStreamKafkaSinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(AxonStreamKafkaSinkApplication.class, args);
    }

    @Bean
    public SpringMessageEventMessageConverter springMessageEventMessageConverter() {
        return new DefaultSpringMessageEventMessageConverter(JacksonSerializer.builder().build(), SequentialPerAggregatePolicy.instance());
    }

    @Bean
    @Qualifier("kafkaMessageSource")
    MessageHandler kafkaMessageSource(SpringMessageEventMessageConverter springMessageEventMessageConverter) {

        return new MessageHandler(springMessageEventMessageConverter);
    }

    @Bean
    @Qualifier("multiMessageSource")
    MultiSubscribableMessageSource multiMessageSource(MessageHandler amqpMessageSource, EventStore eventStore) {

        return new MultiSubscribableMessageSource(Arrays.asList(amqpMessageSource, eventStore));
    }

    @Bean
    public IntegrationFlow flow(MessageHandler kafkaMessageSource) {
        return IntegrationFlows.from(Sink.INPUT)
                               .handle(kafkaMessageSource)
                               .get();
    }

    @Autowired
    public void configure(EventProcessingConfigurer config) {
        config.registerDefaultHandlerInterceptor((configuration, s) -> new LoggingInterceptor(s));
    }
}

