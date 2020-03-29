package org.axonframework.extensions.cloud.stream.demo.coreapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class TaskAssignedEvent implements Serializable {

    private final String id;
    private final String task;

    @JsonCreator
    public TaskAssignedEvent(@JsonProperty("id") String id
            , @JsonProperty("name") String task) {
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
