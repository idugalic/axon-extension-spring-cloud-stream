# Axon Spring Cloud Stream Extension

[Axon](https://axoniq.io/) includes both a programming model as well as specialized infrastructure to provide enterprise ready operational support for the programming model - especially for scaling and distributing mission critical business applications.

The programming model is provided by the popular [Axon Framework](https://axoniq.io/product-overview/axon-framework) while [Axon Server](https://axoniq.io/product-overview/axon-server) is the infrastructure part of Axon.


Spring Cloud Stream is built on the concepts and patterns defined by [Enterprise Integration Patterns](http://www.enterpriseintegrationpatterns.com/) and relies in its internal implementation on an already established and popular implementation of Enterprise Integration Patterns within the Spring portfolio of projects: [Spring Integration](https://projects.spring.io/spring-integration/) framework.

So its only natural for it to support the foundation, semantics, and configuration options that are already established by Spring Integration


Spring Cloud Stream already provides binding interfaces for typical message exchange contracts, which include:


1. `Source`: Message producer (provides the destination to which the produced message (`event`) is sent)
2. `Sink`: Message consumer (provides the destination from which the message (`event`) is consumed)
3. `Processor`: Encapsulates both the `sink` and the `source` contracts by exposing two destinations that allow consumption and production of messages (`events`).

This Axon extension provides implementation of these contracts so your Axon application can act as `Sink` (handling `events` from RabbitMQ, Kafka, other binders supported by Spring Cloud Stream), `Source` (forwarding `events` to RabbitMQ, Kafka, other binders supported by Spring Cloud Stream) or both (as `Processor`).

## Sink

Attaching the amqpMessageSource/message handler to `INPUT` channel of `Sink`

```java
@EnableBinding(Sink.class)
public class Configuration {

    @Bean
    public IntegrationFlow flow(MessageHandler amqpMessageSource) {
        return IntegrationFlows.from(Sink.INPUT)
                               .handle(amqpMessageSource)
                               .get();
    }
    
    ...
    
}
```

Axon [`MessageHandler`](axon-spring-cloud-stream/src/main/java/org/axonframework/extensions/stream/outbound/MessageHandler.java) (amqpMessageSource) is a concrete implementation of `SubscribableMessageSource<EventMessage<?>>` interface provided by Axon Framework. Additionally, it extends `AbstractMessageHandler` provided by Spring Integration so the `Sink.INPUT` can be attached to it, in order to retrieve messages. 
You can define Spring bean of this type in your Configuration:
```java
    ...
    
    @Bean
    @Qualifier("amqpMessageSource")
    MessageHandler amqpMessageSource(SpringMessageEventMessageConverter springMessageEventMessageConverter) {

        return new MessageHandler(springMessageEventMessageConverter);
    }
```

[Event Processors](https://docs.axoniq.io/reference-guide/configuring-infrastructure-components/event-processing/event-processors) are the components that take care of the technical aspects of message (`event`) processing.

Event Processors come in roughly two forms: Subscribing and Tracking. The Subscribing Event Processors subscribe themselves to a `SubscribableMessageSource` of Events and are invoked by the thread managed by the publishing mechanism. Tracking Event Processors, on the other hand, pull their messages from a `StreamableMessageSource` using a thread that it manages itself.

>`StreamableMessageSource` and Tracking Event Processors are preferred due to the multi-threaded options you get with it.
RabbitMQ is more of Subscribable nature. Kafka is more of Streamable nature, but Spring Cloud Stream does not provide programmatic way of handling Kafka `seek/offset` at the moment.

>**This extension is supporting only `SubscribableMessageSource` at the moment.**

To assign event handlers to named processors you should name your processor with the `@ProcessingGroup` annotation

```java
@Component
@ProcessingGroup("StudentEventHandlerAMQP")
public class StudentEventHandlerAMQP {

    @EventHandler
    public void on(StudentCreatedEvent event) {
        System.out.println("AMQP - Id in sink : " + event.getId());
    }

}
```
and register/subscribe this processor to your subscribing message source:
```
axon:
  eventhandling:
    processors:
      StudentEventHandlerAMQP:
        mode: subscribing
        source: amqpMessageSource

```

This extension provides the concept of [MultiSubscribableMessageSource](axon-spring-cloud-stream/src/main/java/org/axonframework/extensions/stream/outbound/MultiSubscribableMessageSource.java) which allows you to register processors on many message sources at the same time.
**This is a powerful feature which enables you to subscribe to Axon Server Event Store and RabbitMQ/Kafka simultaneously, providing a strangling migration path from one infrastructural component (for example: RabbitMQ) to another (for example: Axon Server), or making them work together in the overall systems landscape.**

### Demos

 - [axon-spring-cloud-stream-demo-kafka-sink](axon-spring-cloud-stream-demo/axon-spring-cloud-stream-demo-sink/axon-spring-cloud-stream-demo-kafka-sink)
 - [axon-spring-cloud-stream-demo-rabbitmq-sink](axon-spring-cloud-stream-demo/axon-spring-cloud-stream-demo-sink/axon-spring-cloud-stream-demo-rabbitmq-sink)


## Source

Attaching the `OUTPUT` channel of `Sink` to message producer

```java
@EnableBinding(Source.class)
public class Configuration {

    @Bean
    public IntegrationFlow flow(MessageProducer messageProducer) {
        return IntegrationFlows.from(messageProducer)
                               .channel(Source.OUTPUT)
                               .get();
    }
    ...
    
}
```
Axon [`MessageProducer`](axon-spring-cloud-stream/src/main/java/org/axonframework/extensions/stream/inbound/MessageProducer.java) extends `MessageProducerSupport` provided by Spring integration to forward messages from the given `messageSource` to the given `OUTPUT channel`.
This MessageProducer supports only `SubscribableMessageSource` at the moment. You can set any `SubscribableMessageSource`, including `EventBus`.

```java
...

    @Bean
    public MessageProducer messageProducer(EventBus eventBus, SpringMessageEventMessageConverter converter) {
        return new MessageProducer(eventBus, converter);
    }
```


### Demos

 - [axon-spring-cloud-stream-demo-kafka-source](axon-spring-cloud-stream-demo/axon-spring-cloud-stream-demo-source/axon-spring-cloud-stream-demo-kafka-source)
 - [axon-spring-cloud-stream-demo-rabbitmq-source](axon-spring-cloud-stream-demo/axon-spring-cloud-stream-demo-source/axon-spring-cloud-stream-demo-rabbitmq-source)

Demos are Dockerized and you can run them in the following way:

```bash
mvn clean verify jib:dockerBuild
```
```bash
docker-compose -f .docker/docker-compose-kafka-source-sink.yml up -d
```
or
```bash
docker-compose -f .docker/docker-compose-rabbitmq-source-sink.yml up -d
```

This demo will run two Axon applications (`sink` and `source`) that that are connected to different Axon Servers in order to demonstrate that `sink` application will subscribe to events that are published/forwarded to RabbitMQ/Kafka from the `source` application.

## Processor

Attaching the message producing **handler** to `INPUT` channel of `Processor`, and `OUTPUT` channel of `Processor` to message **producing** handler

```java
@EnableBinding(Processor.class)
public class Configuration {

    @Bean
    public IntegrationFlow flow(MessageProducingHandler amqpMessageSource) {
        return IntegrationFlows.from(Processor.INPUT)
                               .handle(amqpMessageSource)
                               .channel(Processor.OUTPUT)
                               .get();
    }

    ...
    
}
```
Axon [`MessageProducingHandler`](axon-spring-cloud-stream/src/main/java/org/axonframework/extensions/stream/processor/MessageProducingHandler.java) (amqpMessageSource) is a concrete implementation of `SubscribableMessageSource<EventMessage<?>>` interface provided by Axon Framework. Additionally, it extends `AbstractMessageProducingHandler` provided by Spring Integration so the `Processor.INPUT` and `Processor.OUTPUT` can be attached to it, in order to retrieve/send messages from/to this channels. 
This MessageProducingHandler is a message producer and message source at the same time. It supports only SubscribableMessageSource at the moment. You can set any SubscribableMessageSource to subscribe to and forward/produce messages, including EventBus
You can define Spring bean of this type in your Configuration:
```java
    ...
    
    @Bean
    @Qualifier("amqpMessageSource")
    MessageProducingHandler amqpMessageSource(EventBus eventBus, SpringMessageEventMessageConverter springMessageEventMessageConverter) {

        return new MessageProducingHandler(eventBus, springMessageEventMessageConverter);
    }
```
To assign event handlers to named processors you should name your processor with the `@ProcessingGroup` annotation

```java
@Component
@ProcessingGroup("StudentEventHandlerAMQP")
public class StudentEventHandlerAMQP {

    @EventHandler
    public void on(StudentCreatedEvent event) {
        System.out.println("AMQP - Id in sink : " + event.getId());
    }

}
```
and register/subscribe this processor to your subscribing message source:
```
axon:
  eventhandling:
    processors:
      StudentEventHandlerAMQP:
        mode: subscribing
        source: amqpMessageSource

```
### Demos

 - [axon-spring-cloud-stream-demo-kafka-processor](axon-spring-cloud-stream-demo/axon-spring-cloud-stream-demo-processor/axon-spring-cloud-stream-demo-kafka-processor)
 - [axon-spring-cloud-stream-demo-rabbitmq-processor](axon-spring-cloud-stream-demo/axon-spring-cloud-stream-demo-processor/axon-spring-cloud-stream-demo-rabbitmq-processor)

Demos are Dockerized and you can run them in the following way:

```bash
mvn clean verify jib:dockerBuild
```
```bash
docker-compose -f .docker/docker-compose-kafka-processor.yml up -d
```
or
```bash
docker-compose -f .docker/docker-compose-rabbitmq-processor.yml up -d
```

This demo will run one Axon application that is connected to Axon Server to distribute messages (commands, queries and events). It will forward all events that are published to Axon Server Event Bus (`SubscribableMessageSource`) to RabbitMQ/Kafka channels, and subscribe to events from RabbitMQ/Kafka in addition.

## Different Binders

[Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) supports a variety of binder implementations and the following table includes the link to the GitHub projects.

 - RabbitMQ
 - Apache Kafka
 - Kafka Streams
 - Amazon Kinesis
 - Google PubSub (partner maintained)
 - Solace PubSub+ (partner maintained)
 - Azure Event Hubs (partner maintained)
 - Apache RocketMQ (partner maintained)
 
This extension will enable faster integration to all of them. 

Spring Cloud Stream provides additional abstraction layer on top of this integrations and in some cases this can introduce some limitations.  
For example, it does not provide programmatic way of managing Kafka `offset/seek` and because of that Axon is limited to usage of `SubscribableMessageSource`s.

- [Axon Kafka extension](https://docs.axoniq.io/reference-guide/extensions/kafka) is using Spring Kafka directly and we do not have this limitation. In this case we are using `StreamableMessageSource` which we can scale better.
- [Axon Spring AMQP extension](https://docs.axoniq.io/reference-guide/extensions/spring-amqp) is already using `SubscribableMessageSource`s because of the subscribable nature of RabbitMQ, and in that respect Axon Spring AMQP extension can be replaced with this extension without side effects.

Try other binders and let us know how it fits your case !?

## References and Further reading

 - https://docs.axoniq.io/reference-guide/
 - https://github.com/mehdichitforoosh/axon-stream-source
 - https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/_programming_model.html

## Thanks

Many thanks to [`Mehdi Chitforoosh`](https://github.com/mehdichitforoosh/), who is the initial author of this potential Axon Extension.
You can track the [Pull Request](https://github.com/AxonFramework/extension-springcloud/pull/5)

The idea of this project is to get more familiar with Axon Framework concepts (message source, event processors, event handlers) and to demonstrate how this concepts can fit best with Spring Cloud Stream project.
Ideally, similar extensions can be created to support different type of connectors and/or JVM frameworks.

 
