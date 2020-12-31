package com.landedexperts.letlock.chat.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRoomKeyDto {

	public final String roomKey;
	public final String userName;
	public final String token;

	@JsonCreator
	public UserRoomKeyDto(@JsonProperty("roomKey") String roomKey, @JsonProperty("userName") String userName,
			@JsonProperty("token") String token) {
		this.roomKey = roomKey;
		this.userName = userName;
		this.token = token;
	}

	@Override
	public String toString() {
		return "UserRoomKeyDto{" + "roomKey='" + roomKey + '\'' + ", userName='" + userName + '\'' + '}';
	}
}
