package org.axonframework.extensions.cloud.stream.demo.sink.query;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.extensions.cloud.stream.demo.sink.coreapi.StudentCreatedEvent;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("StudentEventHandlerMulti")
public class StudentEventHandlerMulti {

    @EventHandler
    public void on(StudentCreatedEvent event) {
        System.out.println("Multi - Id in sink : " + event.getId());
        System.out.println("Multi - Name in sink: " + event.getName());
        System.out.println("Multi - Age in sink: " + event.getAge());
    }
}
