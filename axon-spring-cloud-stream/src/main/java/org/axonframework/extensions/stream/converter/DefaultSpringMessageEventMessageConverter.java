package org.axonframework.extensions.stream.converter;

import org.axonframework.common.Assert;
import org.axonframework.common.DateTimeUtils;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.GenericDomainEventMessage;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.async.SequencingPolicy;
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy;
import org.axonframework.messaging.Headers;
import org.axonframework.messaging.MetaData;
import org.axonframework.serialization.LazyDeserializingObject;
import org.axonframework.serialization.SerializedMessage;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.SimpleSerializedObject;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.axonframework.common.DateTimeUtils.formatInstant;
import static org.axonframework.messaging.Headers.MESSAGE_TIMESTAMP;

/**
 * @author Mehdi Chitforoosh & Ivan Dugalic
 * @since 4.2
 */
public class DefaultSpringMessageEventMessageConverter implements SpringMessageEventMessageConverter {

    public static final String AXON_MESSAGE_KEY = "axon-message-key";

    private final Serializer serializer;
    private final SequencingPolicy<? super EventMessage<?>> sequencingPolicy;


    public DefaultSpringMessageEventMessageConverter() {
        this.serializer = JacksonSerializer.builder().build();
        this.sequencingPolicy = SequentialPerAggregatePolicy.instance();
    }

    public DefaultSpringMessageEventMessageConverter(final Serializer serializer, final SequencingPolicy<? super EventMessage<?>> sequencingPolicy) {
        Assert.notNull(serializer, () -> "Serializer may not be null");
        Assert.notNull(sequencingPolicy, () -> "Sequencing Policy may not be null");
        this.serializer = serializer;
        this.sequencingPolicy = sequencingPolicy;
    }

    private String sequenceIdentifier(EventMessage<?> eventMessage) {
        Object sequenceIdentifier = sequencingPolicy.getSequenceIdentifierFor(eventMessage);
        return sequenceIdentifier != null ? sequenceIdentifier.toString() : null;
    }

    @Override
    public Message<?> toSpringMessage(EventMessage<?> eventMessage) {
        SerializedObject<byte[]> serializedObject = eventMessage.serializePayload(serializer, byte[].class);
        Map<String, Object> headers = new HashMap<>();
        eventMessage.getMetaData().forEach((k, v) -> headers.put(Headers.MESSAGE_METADATA + "-" + k, v));
        Headers.defaultHeaders(eventMessage, serializedObject).forEach((k, v) -> {
            if (k.equals(MESSAGE_TIMESTAMP)) {
                headers.put(k, formatInstant(eventMessage.getTimestamp()));
            } else {
                headers.put(k, v);
            }
        });
        headers.put(AXON_MESSAGE_KEY, sequenceIdentifier(eventMessage));
        return new GenericMessage<>(serializedObject.getData(),
                                    new DefaultSpringMessageEventMessageConverter.SettableTimestampMessageHeaders(
                                            headers,
                                            eventMessage.getTimestamp().toEpochMilli()));
    }

    @Override
    public EventMessage<?> toEventMessage(Message<?> message) {
        if (!(message.getPayload() instanceof byte[])) {
            throw new IllegalArgumentException("message payload should be byte[]");
        }
        MessageHeaders headers = message.getHeaders();
        if (!headers.keySet().containsAll(Arrays.asList(Headers.MESSAGE_ID, Headers.MESSAGE_TYPE))) {
            throw new IllegalArgumentException("axon message id or axon message type doesn't exist.");
        }
        byte[] payload = (byte[]) message.getPayload();
        Map<String, Object> metaData = new HashMap<>();
        headers.forEach((k, v) -> {
            if (k.startsWith(Headers.MESSAGE_METADATA + "-")) {
                metaData.put(k.substring((Headers.MESSAGE_METADATA + "-").length()), v);
            }
        });
        SimpleSerializedObject<byte[]> serializedMessage = new SimpleSerializedObject<>(payload, byte[].class,
                                                                                        Objects.toString(headers.get(
                                                                                                Headers.MESSAGE_TYPE)),
                                                                                        Objects.toString(headers.get(
                                                                                                Headers.MESSAGE_REVISION),
                                                                                                         null));
        SerializedMessage<?> delegateMessage = new SerializedMessage<>(Objects.toString(headers.get(Headers.MESSAGE_ID)),
                                                                       new LazyDeserializingObject<>(serializedMessage,
                                                                                                     serializer),
                                                                       new LazyDeserializingObject<>(MetaData.from(
                                                                               metaData)));
        String timestamp = Objects.toString(headers.get(MESSAGE_TIMESTAMP));
        if (headers.containsKey(Headers.AGGREGATE_ID)) {
            return new GenericDomainEventMessage<>(Objects.toString(headers.get(Headers.AGGREGATE_TYPE)),
                                                   Objects.toString(headers.get(Headers.AGGREGATE_ID)),
                                                   (Long) headers.get(Headers.AGGREGATE_SEQ),
                                                   delegateMessage, () -> DateTimeUtils.parseInstant(timestamp));
        } else {
            return new GenericEventMessage<>(delegateMessage, () -> DateTimeUtils.parseInstant(timestamp));
        }
    }

    private static class SettableTimestampMessageHeaders extends MessageHeaders {

        protected SettableTimestampMessageHeaders(Map<String, Object> headers, Long timestamp) {
            super(headers, null, timestamp);
        }
    }
}
