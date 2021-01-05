package com.landedexperts.letlock.chat.dto;

import java.util.Objects;

public class SimpleRoomDto {

    @Override
	public String toString() {
		return "SimpleRoomDto [name=" + name + ", key=" + key + ", subscribed=" + subscribed + "]";
	}

	public final String name, key;
    public final boolean subscribed;

    public SimpleRoomDto(String name, String key) {
        this.name = name;
        this.key = key;
        this.subscribed = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleRoomDto that = (SimpleRoomDto) o;
        return subscribed == that.subscribed &&
                Objects.equals(name, that.name) &&
                Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, key, subscribed);
    }
}
