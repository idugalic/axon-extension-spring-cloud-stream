package org.axonframework.extensions.cloud.stream.demo.source.coreapi;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class CreateStudentCommand {

    @TargetAggregateIdentifier
    private final String id;
    private final String name;
    private final String age;

    public CreateStudentCommand(String id, String name, String age) {
        this.id = id;
        this.name = name;
        this.age = age;
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
