package com.landedexperts.letlock.chat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private final Logger logger = LoggerFactory.getLogger(RoomService.class);

	public RoomService() {
		this.roomList = List.of(defaultRoom());
	}

	public List<SimpleRoomDto> roomList(java.util.Set<String> userRooms) {
		List<SimpleRoomDto> roomsList = getRoomList(userRooms).map(room -> room.asSimpleRoomDto());
		if (logger.isDebugEnabled()) {
			logger.debug("Existing rooms room key");
			roomList.forEach(room -> logger.debug(room.key));
		}
		return roomsList;
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

	// TODO: this method and removeUserFromRoom (above) do the same thing for most
	// parts. They need to be integrated into
	// one and the usage for User logout and disconnection use one method instead.
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

	private synchronized List<Room> getRoomList(java.util.Set<String> userRoomKeys) {

		List<Room> userRooms = roomList.filter(room -> userRoomKeys.contains(room.key));
		return userRooms;
	}

	private synchronized List<Room> addRoom(Room roomToAdd) {
		List<Room> ifExist = roomList.toJavaParallelStream().filter(room -> room.key.equals(roomToAdd.key))
				.collect(List.collector());
		if (logger.isDebugEnabled()) {
			logger.debug("in addRoom - Existing rooms room key");
			roomList.forEach(room -> logger.debug(room.key));
			logger.debug("Room to be added: " + roomToAdd.key);
		}

		if (ifExist.size() == 0) {
			logger.debug("The room did not exist adding it.");
			return this.roomList = this.roomList.append(roomToAdd);
		} else {
			logger.debug("Room did exists, skiping adding!!!");
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
		return roomList.exists(room -> room.key.equals(userRoomKey.roomKey));

	}
}
