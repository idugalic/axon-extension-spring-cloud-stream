package org.axonframework.extensions.stream.inbound;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.extensions.stream.converter.DefaultSpringMessageEventMessageConverter;
import org.axonframework.extensions.stream.converter.SpringMessageEventMessageConverter;
import org.axonframework.messaging.SubscribableMessageSource;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author Mehdi Chitforoosh
 * @since 4.1
 */
public class MessageProducer extends MessageProducerSupport {

    private final Predicate<? super EventMessage<?>> filter;
    private final SubscribableMessageSource<EventMessage<?>> messageSource;
    private final SpringMessageEventMessageConverter converter;

    /**
     * Initialize an adapter to forward messages from the given {@code messageSource} to the given {@code channel}.
     * Messages are not filtered; all messages are forwarded to the MessageChannel
     *
     * @param messageSource The event bus to subscribe to.
     */
    public MessageProducer(SubscribableMessageSource<EventMessage<?>> messageSource) {
        this(messageSource, m -> true, new DefaultSpringMessageEventMessageConverter());
    }

    /**
     * Initialize an adapter to forward messages from the given {@code messageSource} to the given {@code channel}.
     * Messages are not filtered; all messages are forwarded to the MessageChannel
     *
     * @param messageSource The event bus to subscribe to.
     * @param converter     The converter to use to convert event message into Spring message
     */
    public MessageProducer(SubscribableMessageSource<EventMessage<?>> messageSource,
                           SpringMessageEventMessageConverter converter) {
        this(messageSource, m -> true, converter);
    }

    /**
     * Initialize an adapter to forward messages from the given {@code messageSource} to the given {@code channel}.
     * Messages are filtered using the given {@code filter}.
     *
     * @param messageSource The source of messages to subscribe to.
     * @param filter        The filter that indicates which messages to forward.
     */
    public MessageProducer(SubscribableMessageSource<EventMessage<?>> messageSource,
                           Predicate<? super EventMessage<?>> filter) {
        this(messageSource, filter, new DefaultSpringMessageEventMessageConverter());
    }

    /**
     * Initialize an adapter to forward messages from the given {@code messageSource} to the given {@code channel}.
     * Messages are filtered using the given {@code filter}.
     *
     * @param messageSource The source of messages to subscribe to.
     * @param filter        The filter that indicates which messages to forward.
     * @param converter     The converter to use to convert event message into Spring message
     */
    public MessageProducer(SubscribableMessageSource<EventMessage<?>> messageSource,
                           Predicate<? super EventMessage<?>> filter, SpringMessageEventMessageConverter converter) {
        this.messageSource = messageSource;
        this.filter = filter;
        this.converter = converter;
    }

    /**
     * Subscribes this event listener to the event bus.
     */
    @Override
    protected void onInit() {
        super.onInit();
        this.messageSource.subscribe(this::handle);
    }


    /**
     * If allows by the filter, wraps the given {@code event} in a {@link GenericMessage} ands sends it to the
     * configured {@link MessageChannel}.
     *
     * @param events the events to handle
     */
    protected void handle(List<? extends EventMessage<?>> events) {
        events.stream()
              .filter(this.filter)
              .forEach(event -> this.sendMessage(this.converter.toSpringMessage(event)));
    }
}
