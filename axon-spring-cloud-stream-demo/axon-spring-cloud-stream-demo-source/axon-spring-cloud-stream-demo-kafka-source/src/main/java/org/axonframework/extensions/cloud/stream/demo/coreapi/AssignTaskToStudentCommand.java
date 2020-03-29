package org.axonframework.extensions.cloud.stream.demo.coreapi;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class AssignTaskToStudentCommand {

    @TargetAggregateIdentifier
    private final String id;
    private final String task;

    public AssignTaskToStudentCommand(String id, String task) {
        this.id = id;
        this.task = task;
    }

    public String getId() {
        return id;
    }

    public String getTask() {
        return task;
    }
}
