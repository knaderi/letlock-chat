package com.landedexperts.letlock.chat.controller;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.landedexperts.letlock.chat.service.RoomService;
import com.landedexperts.letlock.chat.user.User;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;

@Controller
public class ChatController {

	@Value("${letlock.filetransfer.backend.login.url}")
	private String writeTimeOut;

	private static final Logger log = LoggerFactory.getLogger(ChatController.class);

	private final RoomService roomService;
	private final SimpMessageSendingOperations messagingTemplate;

	public ChatController(RoomService roomService, SimpMessageSendingOperations messagingTemplate) {
		this.roomService = roomService;
		this.messagingTemplate = messagingTemplate;
	}

	@SubscribeMapping("/chat/roomList")
	public List<SimpleRoomDto> roomList() {
		return roomService.roomList();
	}

	@MessageMapping("/chat/addRoom")
	@SendTo("/chat/newRoom")
	public SimpleRoomDto addRoom(NewRoomDto newRoom) {
		// TODO: check if the token is valid and belongs to an authenticated user with a
		// file transfer uuid matching roomId
		// call backend, get the username for the token and check against file transfer
		// uuid. If it matches then allow adding room.

		log.debug("Adding room");
		return roomService.addRoom(newRoom.roomName);
	}
	
	@MessageMapping("/chat/removeRoom")
	@SendTo("/chat/removeRoom")
	public void removeRoom(UserRoomKeyDto userRoomKey) {
		// TODO: check if the token is valid and belongs to an authenticated user with a
		// file transfer uuid matching roomId
		// call backend, get the username for the token and check against file transfer
		// uuid. If it matches then allow removing room.

		log.debug("Removing room");
		roomService.removeRoom(userRoomKey);
	}

	@MessageMapping("/chat/{roomId}/join")
	public ChatRoomUserListDto userJoinRoom(UserRoomKeyDto userRoomKey, SimpMessageHeaderAccessor headerAccessor) {
		// TODO: check if the token, username and file transferuuid are valid and then
		// allow the rest of the call goahead
		// adding the user to room. Update the has in roomService with roomId and token

//        with enabled spring security
//        final String securityUser = headerAccessor.getUser().getName();
		final String username = (String) headerAccessor.getSessionAttributes().put("username", userRoomKey.userName);
		final Message joinMessage = new Message(MessageTypes.JOIN, userRoomKey.userName, "", "");
		return roomService.addUserToRoom(userRoomKey).map(userList -> {
			messagingTemplate.convertAndSend(format("/chat/%s/userList", userList.roomKey), userList);
			sendMessage(userRoomKey.roomKey, joinMessage, userRoomKey.token);
			return userList;
		}).getOrElseGet(appError -> {
			log.error("invalid room id...");
			return new ChatRoomUserListDto(userRoomKey.roomKey, HashSet.empty());
		});
	}

	@MessageMapping("/chat/{roomId}/leave")
	public ChatRoomUserListDto userLeaveRoom(UserRoomKeyDto userRoomKey, SimpMessageHeaderAccessor headerAccessor) {
		// TODO: check if the token, username and file transferuuid are valid and then
		// allow the rest of the call remove the user from the room

		final Message leaveMessage = new Message(MessageTypes.LEAVE, userRoomKey.userName, "", "");
		return roomService.removeUserFromRoom(userRoomKey).map(userList -> {
			messagingTemplate.convertAndSend(format("/chat/%s/userList", userList.roomKey), userList);
			sendMessage(userRoomKey.roomKey, leaveMessage, userRoomKey.token);
			return userList;
		}).getOrElseGet(appError -> {
			log.error("invalid room id...");
			return new ChatRoomUserListDto(userRoomKey.roomKey, HashSet.empty());
		});
	}

	@MessageMapping("chat/{roomId}/sendMessage")
	public Message sendMessage(@DestinationVariable String roomId, Message message, String token) {
		//TODO: check the map in roomservice to see if the token for the roomId exist, then allow continuing
		messagingTemplate.convertAndSend(format("/chat/%s/messages", roomId), message);
		return message;
	}

	public void handleUserDisconnection(String userName) {
		final User user = new User(userName);
		final Message leaveMessage = new Message(MessageTypes.LEAVE, userName, "", "");
		List<Room> userRooms = roomService.disconnectUser(user);
		userRooms.map(room -> new ChatRoomUserListDto(room.key, room.users)).forEach(roomUserList -> {
			messagingTemplate.convertAndSend(format("/chat/%s/userList", roomUserList.roomKey), roomUserList);
			sendMessage(roomUserList.roomKey, leaveMessage, "");
		});
	}

}
