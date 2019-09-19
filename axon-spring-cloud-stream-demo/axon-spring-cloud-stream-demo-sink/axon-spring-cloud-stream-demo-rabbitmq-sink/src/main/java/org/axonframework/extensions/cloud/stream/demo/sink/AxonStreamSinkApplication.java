package org.axonframework.extensions.cloud.stream.demo.sink;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.extensions.stream.converter.DefaultSpringMessageEventMessageConverter;
import org.axonframework.extensions.stream.converter.SpringMessageEventMessageConverter;
import org.axonframework.extensions.stream.outbound.MessageHandler;
import org.axonframework.extensions.stream.outbound.MultiSubscribableMessageSource;
import org.axonframework.messaging.interceptors.LoggingInterceptor;
import org.axonframework.serialization.Serializer;
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
public class AxonStreamSinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(AxonStreamSinkApplication.class, args);
    }

    @Bean
    public SpringMessageEventMessageConverter springMessageEventMessageConverter(Serializer serializer) {
        return new DefaultSpringMessageEventMessageConverter(serializer);
    }

    // Defining the `amqpMessageSource` Spring bean.
    // You can register/subscribe different Axon Event (Subscribing) Processors to this message source
    // Example: src/main/resources/application/yml
    //_____________________________________________
    // axon:
    //   eventhandling:
    //     processors:
    //       StudentEventHandlerAMQP:
    //         mode: subscribing
    //         source: amqpMessageSource
    @Bean
    @Qualifier("amqpMessageSource")
    MessageHandler amqpMessageSource(SpringMessageEventMessageConverter springMessageEventMessageConverter) {

        return new MessageHandler(springMessageEventMessageConverter);
    }

    @Bean
    @Qualifier("multiMessageSource")
    MultiSubscribableMessageSource multiMessageSource(MessageHandler amqpMessageSource, EventStore eventStore) {

        return new MultiSubscribableMessageSource(Arrays.asList(amqpMessageSource, eventStore));
    }


    // Attaching the INPUT channel of Sink to amqpMessageSource/message handler
    @Bean
    public IntegrationFlow flow(MessageHandler amqpMessageSource) {
        return IntegrationFlows.from(Sink.INPUT)
                               .handle(amqpMessageSource)
                               .get();
    }

    @Autowired
    public void configure(EventProcessingConfigurer config) {
        config.registerDefaultHandlerInterceptor((configuration, s) -> new LoggingInterceptor(s));
    }

//    @Bean
//    CommandLineRunner run(CommandBus commandBus) {
//
//        return new CommandLineRunner() {
//            @Override
//            public void run(String... args) throws Exception {
//                for (int i = 0; i < 10; i++) {
//                    commandBus.dispatch(GenericCommandMessage.asCommandMessage(new CreateStudentCommand(UUID.randomUUID().toString(), "mehdi", "32")));
//                    commandBus.dispatch(GenericCommandMessage.asCommandMessage(new CreateStudentCommand(UUID.randomUUID().toString(), "ali", "32")));
//                }
//            }
//        };
//    }
}

