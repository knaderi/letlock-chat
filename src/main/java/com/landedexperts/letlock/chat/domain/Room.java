package com.landedexperts.letlock.chat.domain;

import com.landedexperts.letlock.chat.dto.SimpleRoomDto;
import com.landedexperts.letlock.chat.user.User;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class Room {


	public final String name;
    public final String key;
    public final Set<User> users;

    public Room(String name) {
        this.name = name;
        this.key = generateKey(name);
        this.users = HashSet.empty();
    }

    private Room(String name, String key, Set<User> users) {
        this.name = name;
        this.key = key;
        this.users = users;
    }

    public Room subscribe(User user) {
        final Set<User> subscribedUsers = this.users.add(user);
        return new Room(this.name, this.key, subscribedUsers);
    }

    public Room unsubscribe(User user) {
        final Set<User> subscribedUsers = this.users.remove(user);
        return new Room(this.name, this.key, subscribedUsers);
    }

    public SimpleRoomDto asSimpleRoomDto() {
        return new SimpleRoomDto(this.name, this.key);
    }

    private String generateKey(String roomName) {
        return roomName.toLowerCase().trim().replaceAll("\\s+", "-");
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Room other = (Room) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
    @Override
	public String toString() {
		return "Room [name=" + name + ", key=" + key + ", users=" + users + "]";
	}
}
