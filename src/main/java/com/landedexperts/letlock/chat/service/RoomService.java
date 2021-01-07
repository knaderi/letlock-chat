package com.landedexperts.letlock.chat.service;

import org.springframework.stereotype.Service;

import com.landedexperts.letlock.chat.app.AppError;
import com.landedexperts.letlock.chat.domain.Room;
import com.landedexperts.letlock.chat.dto.ChatRoomUserListDto;
import com.landedexperts.letlock.chat.dto.SimpleRoomDto;
import com.landedexperts.letlock.chat.dto.UserRoomKeyDto;
import com.landedexperts.letlock.chat.user.User;

import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.control.Either;

@Service
public class RoomService {

	private List<Room> roomList;

	public RoomService() {
		this.roomList = List.of(defaultRoom());
	}

	public List<SimpleRoomDto> roomList(String userName) {
		return getRoomList(userName).map(room -> room.asSimpleRoomDto());
	}

	public SimpleRoomDto addRoom(String roomName) {
		final Room room = new Room(roomName);
		roomList = addRoom(room);
		return room.asSimpleRoomDto();
	}

	public void removeRoom(String roomName) {
		this.roomList = remove(roomName);
	}

	
	public Either<AppError, ChatRoomUserListDto> usersInChatRoom(String roomKey) {
		return this.roomList.find(room -> room.key.equals(roomKey))
				.map(room -> new ChatRoomUserListDto(room.key, room.users)).toEither(AppError.INVALID_ROOM_KEY);
	}

	public Either<AppError, ChatRoomUserListDto> addUserToRoom(UserRoomKeyDto userRoomKey) {
		final User user = new User(userRoomKey.userName);
		this.roomList.find(room -> room.key.equals(userRoomKey.roomKey)).map(oldRoom -> {
			final Room newRoom = oldRoom.subscribe(user);
			updateRoom(oldRoom, newRoom);
			return newRoom;
		});
		return usersInChatRoom(userRoomKey.roomKey);
	}
	
	public Set<User> getUserList(String roomKey) {

		Room theRoom = this.roomList.find(room -> room.key.equals(roomKey)).collect(List.collector()).get();

		return theRoom.users;
	}

	public Either<AppError, ChatRoomUserListDto> removeUserFromRoom(UserRoomKeyDto userRoomKey) {
		final User user = new User(userRoomKey.userName);
		this.roomList.find(room -> room.key.equals(userRoomKey.roomKey)).map(oldRoom -> {
			final Room newRoom = oldRoom.unsubscribe(user);
			updateRoom(oldRoom, newRoom);
			return newRoom;
		});
		return usersInChatRoom(userRoomKey.roomKey);
	}

	public List<Room> disconnectUser(User user) {
		final List<Room> userRooms = this.roomList.filter(room -> room.users.contains(user)).map(oldRoom -> {
			final Room newRoom = oldRoom.unsubscribe(user);
			updateRoom(oldRoom, newRoom);
			return newRoom;
		});

		return userRooms;
	}

	private Room defaultRoom() {
		return new Room("Main room");
	}

	private synchronized List<Room> getRoomList(String userName) {
		User user = new User(userName);
		List<Room> userRooms = roomList.filter(room -> room.users.contains(user));
		return userRooms;
	}

	private synchronized List<Room> addRoom(Room roomToAdd) {
		List<Room> ifExist = roomList.toJavaParallelStream().filter(room -> room.key.equals(roomToAdd.key)).collect(List.collector());
		if(ifExist.size() == 0) {
		    return this.roomList = this.roomList.append(roomToAdd);
		}else {
			return this.roomList;
		}
	}

	private synchronized List<Room> remove(String roomName) {
		return roomList.toJavaParallelStream().filter(room -> !room.name.equals(roomName)).collect(List.collector());

	}

	private synchronized List<Room> updateRoom(Room oldRoom, Room newRoom) {
		return this.roomList = this.roomList.remove(oldRoom).append(newRoom);
	}

	public boolean roomExist(UserRoomKeyDto userRoomKey) {
		return roomList.exists(room->room.key.equals(userRoomKey.roomKey));
		
	}
}
