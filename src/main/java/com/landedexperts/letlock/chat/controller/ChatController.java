package com.landedexperts.letlock.chat.controller;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.landedexperts.letlock.chat.domain.Room;
import com.landedexperts.letlock.chat.dto.ChatRoomUserListDto;
import com.landedexperts.letlock.chat.dto.NewRoomDto;
import com.landedexperts.letlock.chat.dto.SimpleRoomDto;
import com.landedexperts.letlock.chat.dto.UserRoomKeyDto;
import com.landedexperts.letlock.chat.message.Message;
import com.landedexperts.letlock.chat.message.MessageTypes;
import com.landedexperts.letlock.chat.service.LetLockBackendHelper;
import com.landedexperts.letlock.chat.service.RoomService;
import com.landedexperts.letlock.chat.user.User;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;

@Controller
public class ChatController {

	//@Value("${letlock.filetransfer.backend.login.url}")
	private String letlockBackendURI = "http://letlockbackenddev.us-west-2.elasticbeanstalk.com:5000";
	

	private static final Logger log = LoggerFactory.getLogger(ChatController.class);

	private final RoomService roomService;
	private final SimpMessageSendingOperations messagingTemplate;

	public ChatController(RoomService roomService, SimpMessageSendingOperations messagingTemplate) {
		this.roomService = roomService;
		this.messagingTemplate = messagingTemplate;
	}

	@SubscribeMapping("/chat/{userName}/roomList")
	public List<SimpleRoomDto> roomList(@DestinationVariable String userName) {
		return roomService.roomList(userName);
	}

	@MessageMapping("/chat/addRoom")
	@SendTo("/chat/newRoom")
	/**
	 * Convert messages that are headed to /app/addRoom and convert it to a new
	 * chatMessage and send it to "/chat/newRoom", so all subscribers for
	 * "/chat/newRoom" will receiver the message.
	 * 
	 * @param newRoom
	 * @return
	 */
	public SimpleRoomDto addRoom(NewRoomDto newRoom) {
		return roomService.addRoom(newRoom.roomName); // returns where subscribed for the SimpleRoomDto is false. It
														// does not return Room as it does not need the list of users.
	}

	@MessageMapping("/chat/{roomId}/join")
	public ChatRoomUserListDto userJoinRoom(UserRoomKeyDto userRoomKey, SimpMessageHeaderAccessor headerAccessor) {

		if (!roomService.roomExist(userRoomKey)) {
			roomService.addRoom(userRoomKey.roomKey);
		}
		boolean isUserAuthenticatedForRoom = LetLockBackendHelper.getInstance(letlockBackendURI).authenticateForRoom(userRoomKey.token,
				userRoomKey.roomKey);
		if (isUserAuthenticatedForRoom) {
			log.error("User is not authenticated for the room.");
			return new ChatRoomUserListDto(userRoomKey.roomKey, HashSet.empty());
		}
		headerAccessor.getSessionAttributes().put("username", userRoomKey.userName);
		final Message joinMessage = new Message(MessageTypes.JOIN, userRoomKey.userName, "user joined",
				userRoomKey.token);
		return roomService.addUserToRoom(userRoomKey).map(userList -> {
			messagingTemplate.convertAndSend(format("/chat/%s/userList", userList.roomKey), userList);
			sendMessage(userRoomKey.roomKey, joinMessage);
			return userList;
		}).getOrElseGet(appError -> {
			log.error("invalid room id...");
			return new ChatRoomUserListDto(userRoomKey.roomKey, HashSet.empty());
		});
	}

	@MessageMapping("/chat/{roomId}/leave")
	// TODO: shouldn't we have @DestinationVariable String roomId to use it.
	public ChatRoomUserListDto userLeaveRoom(UserRoomKeyDto userRoomKeyDto, SimpMessageHeaderAccessor headerAccessor) {
		final Message leaveMessage = new Message(MessageTypes.LEAVE, userRoomKeyDto.userName, "user left",
				userRoomKeyDto.token);
		return roomService.removeUserFromRoom(userRoomKeyDto).map(userList -> {
			messagingTemplate.convertAndSend(format("/chat/%s/userList", userList.roomKey), userList);
			sendMessage(userRoomKeyDto.roomKey, leaveMessage);
			return userList;
		}).getOrElseGet(appError -> {
			log.error("invalid room id...");
			return new ChatRoomUserListDto(userRoomKeyDto.roomKey, HashSet.empty());
		});
	}

	@MessageMapping("/chat/{roomId}/userList")
	@SendTo("/chat/{roomId}/userList")
	public Set<User> userList(@DestinationVariable String roomId, Message message) {
		return roomService.getUserList(roomId);
	}

	@MessageMapping("/chat/{roomId}/sendMessage")
	public Message sendMessage(@DestinationVariable String roomId, Message message) {
		messagingTemplate.convertAndSend(format("/chat/%s/messages", roomId), message);
		return message;
	}

	public void handleUserDisconnection(String userName) {
		final User user = new User(userName);
		final Message leaveMessage = new Message(MessageTypes.LEAVE, userName, "User disconnedted", "");
		List<Room> userRooms = roomService.disconnectUser(user);
		userRooms.map(room -> new ChatRoomUserListDto(room.key, room.users)).forEach(roomUserList -> {
			messagingTemplate.convertAndSend(format("/chat/%s/userList", roomUserList.roomKey), roomUserList);
			sendMessage(roomUserList.roomKey, leaveMessage);
			messagingTemplate.convertAndSend(format("/chat/{userName}/roomList", userName), userRooms);
		});
		// Remove user's empty rooms
		userRooms.toStream().filter(room -> room.users.isEmpty())
				.forEach(emptyRoom -> roomService.removeRoom(emptyRoom.name));
	}

}
