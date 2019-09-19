package org.axonframework.extensions.stream.outbound;

import org.axonframework.common.Registration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.SubscribableMessageSource;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Ivan Dugalic
 * @since 4.2
 */
public class MultiSubscribableMessageSource implements SubscribableMessageSource<EventMessage<?>> {

    private final CopyOnWriteArrayList<SubscribableMessageSource> subscribableMessageSources = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Registration> registrations = new CopyOnWriteArrayList<>();

    public MultiSubscribableMessageSource(List<SubscribableMessageSource> subscribableMessageSources) {
        this.subscribableMessageSources.addAll(subscribableMessageSources);
    }

    @Override
    public Registration subscribe(Consumer<List<? extends EventMessage<?>>> messageProcessor) {

        registrations = subscribableMessageSources.stream().map(sms -> sms.subscribe(messageProcessor)).collect(
                Collectors.toCollection(CopyOnWriteArrayList::new));

        return () -> registrations.stream().allMatch(Registration::cancel);
    }
}
