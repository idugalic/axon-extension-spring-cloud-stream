package org.axonframework.extensions.cloud.stream.demo.sink.query;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.extensions.cloud.stream.demo.coreapi.StudentCreatedEvent;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("StudentEventHandlerAMQP")
public class StudentEventHandlerAMQP {

    @EventHandler
    public void on(StudentCreatedEvent event) {
        System.out.println("AMQP - Id in sink : " + event.getId());
        System.out.println("AMQP - in sink: " + event.getName());
        System.out.println("AMQP - in sink: " + event.getAge());
    }
}
