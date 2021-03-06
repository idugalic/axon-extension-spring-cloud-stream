package org.axonframework.extensions.cloud.stream.demo.source.query;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.extensions.cloud.stream.demo.coreapi.StudentCreatedEvent;
import org.axonframework.extensions.cloud.stream.demo.coreapi.TaskAssignedEvent;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("StudentEventHandlerAxonServer")
public class StudentEventHandlerAxonServer {

    @EventHandler
    public void on(StudentCreatedEvent event) {
        System.out.println("Axon Server - Id in sink : " + event.getId());
        System.out.println("Axon Server - Name in sink: " + event.getName());
        System.out.println("Axon Server - Age in sink: " + event.getAge());
    }

    @EventHandler
    public void on(TaskAssignedEvent event) {
        System.out.println("Axon Server - Id in sink : " + event.getId());
        System.out.println("Axon Server - Name in sink: " + event.getTask());
    }
}
