package org.axonframework.extensions.stream.outbound;

import org.axonframework.common.Registration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.extensions.stream.converter.SpringMessageEventMessageConverter;
import org.axonframework.messaging.SubscribableMessageSource;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;

/**
 * Axon Message Source, which is actually a Spring Integration message handler
 *
 * @author Mehdi Chitforoosh & Ivan Dugalic
 * @since 4.2
 */
public class MessageHandler extends AbstractMessageHandler implements SubscribableMessageSource<EventMessage<?>> {

    private final CopyOnWriteArrayList<Consumer<List<? extends EventMessage<?>>>> messageProcessors = new CopyOnWriteArrayList<>();
    private final SpringMessageEventMessageConverter eventMessageConverter;


    /**
     * Creates the MessageHandler
     *
     * @param eventMessageConverter The message converter responsible for converting from/to Spring messages
     */
    public MessageHandler(SpringMessageEventMessageConverter eventMessageConverter) {
        this.eventMessageConverter = eventMessageConverter;
    }


    @Override
    public Registration subscribe(Consumer<List<? extends EventMessage<?>>> messageProcessor) {
        messageProcessors.add(messageProcessor);
        return () -> messageProcessors.remove(messageProcessor);
    }

    /**
     * Handles the given {@code message}. If the filter refuses the message, it is ignored.
     *
     * @param message The message containing the event to publish
     */
    @Override
    protected void handleMessageInternal(Message<?> message) throws Exception {
        EventMessage<?> eventMessage = eventMessageConverter.toEventMessage(message);
        List<? extends EventMessage<?>> messages = singletonList(eventMessage);
        for (Consumer<List<? extends EventMessage<?>>> messageProcessor : messageProcessors) {
            messageProcessor.accept(messages);
        }
    }
}
