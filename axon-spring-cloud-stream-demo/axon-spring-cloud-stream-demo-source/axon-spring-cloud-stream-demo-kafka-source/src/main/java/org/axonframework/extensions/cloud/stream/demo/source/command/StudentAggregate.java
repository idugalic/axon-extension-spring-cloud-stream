package org.axonframework.extensions.cloud.stream.demo.source.command;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.extensions.cloud.stream.demo.coreapi.AssignTaskToStudentCommand;
import org.axonframework.extensions.cloud.stream.demo.coreapi.CreateStudentCommand;
import org.axonframework.extensions.cloud.stream.demo.coreapi.StudentCreatedEvent;
import org.axonframework.extensions.cloud.stream.demo.coreapi.TaskAssignedEvent;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;


@Aggregate
public class StudentAggregate {

    @AggregateIdentifier
    private String id;
    private String name;
    private String age;

    @CommandHandler
    public StudentAggregate(CreateStudentCommand command) {
        apply(new StudentCreatedEvent(command.getId(), command.getName(), command.getAge()));
    }

    public StudentAggregate() {
    }

    @CommandHandler
    public void on(AssignTaskToStudentCommand command){
        apply(new TaskAssignedEvent(command.getId(), command.getTask()));
    }

    @EventSourcingHandler
    public void on(StudentCreatedEvent event) {
        this.id = event.getId();
        this.name = event.getName();
        this.age = event.getAge();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }
}
