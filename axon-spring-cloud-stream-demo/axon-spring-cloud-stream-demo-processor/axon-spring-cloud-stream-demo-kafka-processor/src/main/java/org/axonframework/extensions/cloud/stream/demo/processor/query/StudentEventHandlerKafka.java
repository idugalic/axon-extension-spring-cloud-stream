package org.axonframework.extensions.cloud.stream.demo.processor.query;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.extensions.cloud.stream.demo.processor.coreapi.StudentCreatedEvent;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("StudentEventHandlerKafka")
public class StudentEventHandlerKafka {

    @EventHandler
    public void on(StudentCreatedEvent event) {
        System.out.println("Kafka - Id in sink : " + event.getId());
        System.out.println("Kafka - in sink: " + event.getName());
        System.out.println("Kafka - in sink: " + event.getAge());
    }
}
