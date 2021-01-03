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
		return "UserRoomKeyDto [roomKey=" + roomKey + ", userName=" + userName + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roomKey == null) ? 0 : roomKey.hashCode());
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserRoomKeyDto other = (UserRoomKeyDto) obj;
		if (roomKey == null) {
			if (other.roomKey != null)
				return false;
		} else if (!roomKey.equals(other.roomKey))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}




}
