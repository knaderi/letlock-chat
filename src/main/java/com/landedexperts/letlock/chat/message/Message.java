package com.landedexperts.letlock.chat.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {

    public final MessageTypes type;
    public final String userName;
    public final String message;
    public final String token;

    @JsonCreator
    public Message(@JsonProperty("type") MessageTypes type,
                   @JsonProperty("userName") String userName,
                   @JsonProperty("message") String message,
                   @JsonProperty("token") String token) {
        this.type = type;
        this.userName = userName;
        this.message = message;
        this.token = token;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", userName='" + userName + '\'' +
                ", message='" + message + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
