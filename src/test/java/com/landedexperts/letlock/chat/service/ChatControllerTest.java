package com.landedexperts.letlock.chat.service;

import static java.lang.String.format;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import com.landedexperts.letlock.chat.dto.ChatRoomUserListDto;
import com.landedexperts.letlock.chat.dto.UserRoomKeyDto;
import com.landedexperts.letlock.chat.message.Message;
import com.landedexperts.letlock.chat.message.MessageTypes;
import com.landedexperts.letlock.chat.user.User;

import io.vavr.collection.HashSet;

public class ChatControllerTest {

	private static final Logger log = LoggerFactory.getLogger(ChatControllerTest.class);
	private final SimpMessageSendingOperations messagingTemplate = Mockito.mock(SimpMessageSendingOperations.class);

	@Test
	public void addRoomAndUsers() throws Exception {
		RoomService roomService = new RoomService();

		String roomName = "testRoom";
       
		//add room
		roomService.addRoom(roomName);
		String roomKey = "testroom";
		String userName1 = "testUser1";
		String token1 = "token1";

		//add testUser1
		ChatRoomUserListDto chatRoomUserListDto = addUserToRoom(roomService, roomKey, userName1, token1);
		Assert.assertTrue(chatRoomUserListDto.users.size() == 1);
		Assert.assertTrue(chatRoomUserListDto.users.contains(new User(userName1)));

		String userName2 = "testUser2";
		String token2 = "token2";

		//add testUser2
		ChatRoomUserListDto chatRoomUserListDto2 = addUserToRoom(roomService, roomKey, userName2, token2);
		Assert.assertTrue(chatRoomUserListDto2.users.size() == 2);
		Assert.assertTrue(chatRoomUserListDto2.users.contains(new User(userName2)));
		
		//Remover user 1
		ChatRoomUserListDto chatRoomUserListDto3 = userLeaveRoom(roomService, new UserRoomKeyDto(roomKey, userName1, token1));
		Assert.assertTrue(chatRoomUserListDto3.users.size() == 1);
		Assert.assertFalse(chatRoomUserListDto3.users.contains(new User(userName1)));
	}

	private ChatRoomUserListDto addUserToRoom(RoomService roomService, String roomKey, String userName, String token) {
		UserRoomKeyDto userRoomKey = new UserRoomKeyDto(roomKey, userName, token);
		final Message joinMessage = new Message(MessageTypes.JOIN, userRoomKey.userName, "user joined",
				userRoomKey.token);

		ChatRoomUserListDto chatRoomUserListDto = roomService.addUserToRoom(userRoomKey).map(userList -> {
			messagingTemplate.convertAndSend(format("/chat/%s/userList", userList.roomKey), userList);
			sendMessage(userRoomKey.roomKey, joinMessage, userRoomKey.token);
			return userList;
		}).getOrElseGet(appError -> {
			log.error("invalid room id...");
			return new ChatRoomUserListDto(userRoomKey.roomKey, HashSet.empty());
		});
		return chatRoomUserListDto;
	}

	private ChatRoomUserListDto userLeaveRoom(RoomService roomService, UserRoomKeyDto userRoomKeyDto) {
		final Message leaveMessage = new Message(MessageTypes.LEAVE, userRoomKeyDto.userName, "user left", userRoomKeyDto.token);
		ChatRoomUserListDto chatRoomUserListDto = roomService.removeUserFromRoom(userRoomKeyDto).map(userList -> {
			messagingTemplate.convertAndSend(format("/chat/%s/userList", userList.roomKey), userList);
			sendMessage(userRoomKeyDto.roomKey, leaveMessage, userRoomKeyDto.token);
			return userList;
		}).getOrElseGet(appError -> {
			log.error("invalid room id...");
			return new ChatRoomUserListDto(userRoomKeyDto.roomKey, HashSet.empty());
		});
		return chatRoomUserListDto;
	}

	public Message sendMessage(@DestinationVariable String roomId, Message message, String token) {
		// TODO: check the map in roomservice to see if the token for the roomId exist,
		// then allow continuing
		messagingTemplate.convertAndSend(format("/chat/%s/messages", roomId), message);
		return message;
	}

}
