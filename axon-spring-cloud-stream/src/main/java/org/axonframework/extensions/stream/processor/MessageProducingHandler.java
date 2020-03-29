package org.axonframework.extensions.stream.processor;

import org.axonframework.common.Registration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.extensions.stream.converter.DefaultSpringMessageEventMessageConverter;
import org.axonframework.extensions.stream.converter.SpringMessageEventMessageConverter;
import org.axonframework.messaging.SubscribableMessageSource;
import org.springframework.integration.handler.AbstractMessageProducingHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Mehdi Chitforoosh
 * @since 4.2
 */
public class MessageProducingHandler extends AbstractMessageProducingHandler
        implements SubscribableMessageSource<EventMessage<?>> {

    private final Predicate<? super EventMessage<?>> filter;
    private final SubscribableMessageSource<EventMessage<?>> messageSource;
    private final CopyOnWriteArrayList<Consumer<List<? extends EventMessage<?>>>> messageProcessors = new CopyOnWriteArrayList<>();
    private final SpringMessageEventMessageConverter converter;

    /**
     * Initialize a handler to forward messages from the given {@code messageSource} to the given {@code channel}.
     * Messages are not filtered; all messages are forwarded to the MessageChannel
     *
     * @param messageSource The message source (for example: eventBus) to subscribe to.
     */
    public MessageProducingHandler(SubscribableMessageSource<EventMessage<?>> messageSource) {
        this(messageSource, m -> true, new DefaultSpringMessageEventMessageConverter());
    }

    /**
     * Initialize a handler to forward messages from the given {@code messageSource} to the given {@code channel}.
     * Messages are not filtered; all messages are forwarded to the MessageChannel
     *
     * @param messageSource The message source (for example: eventBus) to subscribe to.
     * @param converter     The converter to use to convert event message into Spring message
     */
    public MessageProducingHandler(SubscribableMessageSource<EventMessage<?>> messageSource,
                                   SpringMessageEventMessageConverter converter) {
        this(messageSource, m -> true, converter);
    }

    /**
     * Initialize a handler to forward messages from the given {@code messageSource} to the given {@code channel}.
     * Messages are filtered using the given {@code filter}.
     *
     * @param messageSource The message source (for example: eventBus) to subscribe to.
     * @param filter        The filter that indicates which messages to forward.
     */
    public MessageProducingHandler(SubscribableMessageSource<EventMessage<?>> messageSource,
                                   Predicate<? super EventMessage<?>> filter) {
        this(messageSource, filter, new DefaultSpringMessageEventMessageConverter());
    }

    /**
     * Initialize a handler to forward messages from the given {@code messageSource} to the given {@code channel}.
     * Messages are filtered using the given {@code filter}.
     *
     * @param messageSource The message source (for example: eventBus) to subscribe to.
     * @param filter        The filter that indicates which messages to forward.
     * @param converter     The converter to use to convert event message into Spring message
     */
    public MessageProducingHandler(SubscribableMessageSource<EventMessage<?>> messageSource,
                                   Predicate<? super EventMessage<?>> filter,
                                   SpringMessageEventMessageConverter converter) {
        this.messageSource = messageSource;
        this.filter = filter;
        this.converter = converter;
    }

    // SOURCE

    /**
     * Subscribes this event listener to the message source.
     */
    @Override
    protected void onInit() {
        try {
            super.onInit();
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
        this.messageSource.subscribe(this::handle);
    }


    /**
     * Wraps the given {@code event} in a {@link GenericMessage} and sends it to the configured {@link MessageChannel}.
     *
     * @param events the events to handle
     */
    protected void handle(List<? extends EventMessage<?>> events) {
        events.stream()
              .filter(filter)
              .forEach(event -> this.sendOutput(converter.toSpringMessage(event), null, false));
    }

    // SINK

    /**
     * Handles the given {@code message}. If the filter refuses the message, it is ignored.
     *
     * @param message The message containing the event to publish
     */
    @Override
    protected void handleMessageInternal(Message<?> message) throws Exception {
        EventMessage<?> eventMessage = converter.toEventMessage(message);
        List<? extends EventMessage<?>> messages = Collections.singletonList(eventMessage);
        for (Consumer<List<? extends EventMessage<?>>> messageProcessor : messageProcessors) {
            messageProcessor.accept(messages);
        }
    }


    @Override
    public Registration subscribe(Consumer<List<? extends EventMessage<?>>> messageProcessor) {
        messageProcessors.add(messageProcessor);
        return () -> messageProcessors.remove(messageProcessor);
    }
}
