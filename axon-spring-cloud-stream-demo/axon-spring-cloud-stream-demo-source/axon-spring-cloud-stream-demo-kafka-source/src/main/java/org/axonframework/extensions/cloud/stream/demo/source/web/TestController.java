package org.axonframework.extensions.cloud.stream.demo.source.web;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.extensions.cloud.stream.demo.coreapi.AssignTaskToStudentCommand;
import org.axonframework.extensions.cloud.stream.demo.coreapi.CreateStudentCommand;
import org.axonframework.extensions.stream.converter.SpringMessageEventMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TestController {

    private final CommandGateway commandGateway;

    public TestController(CommandGateway commandGateway,
                          SpringMessageEventMessageConverter springMessageEventMessageConverter) {
        this.commandGateway = commandGateway;
    }

    @GetMapping("/create")
    public String createAxonCommand() {
        return commandGateway.sendAndWait(new CreateStudentCommand(UUID.randomUUID().toString(), "mehdi", "32"));
    }

    @GetMapping("/taskassign/{id}")
    public String assignAxonCommand(@PathVariable String id) {
        return commandGateway.sendAndWait(new AssignTaskToStudentCommand(id, "task1"));
    }
}
