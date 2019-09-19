package org.axonframework.extensions.cloud.stream.demo.source.web;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.extensions.cloud.stream.demo.source.coreapi.CreateStudentCommand;
import org.axonframework.extensions.stream.converter.SpringMessageEventMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TestController {

    private final CommandGateway commandGateway;

    public TestController(CommandGateway commandGateway,
                          SpringMessageEventMessageConverter springMessageEventMessageConverter) {
        this.commandGateway = commandGateway;
    }

    @GetMapping("/axon")
    public String testAxonCommand() {
        return commandGateway.sendAndWait(new CreateStudentCommand(UUID.randomUUID().toString(), "mehdi", "32"));
    }
}
