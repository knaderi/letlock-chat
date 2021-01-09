package com.landedexperts.letlock.chat.dto;

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
		SimpleRoomDto other = (SimpleRoomDto) obj;
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

  
}
