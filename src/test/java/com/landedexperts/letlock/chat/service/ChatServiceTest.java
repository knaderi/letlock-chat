package com.landedexperts.letlock.chat.service;

import org.junit.Assert;
import org.junit.Test;

import com.landedexperts.letlock.chat.app.AppError;
import com.landedexperts.letlock.chat.dto.ChatRoomUserListDto;
import com.landedexperts.letlock.chat.dto.UserRoomKeyDto;
import com.landedexperts.letlock.chat.user.User;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import io.vavr.control.Either;


public class ChatServiceTest {


	@Test
	public void addRemoveUserToChatRoom() {
		RoomService roomService = new RoomService();
		roomService.addRoom("testroom");
		UserRoomKeyDto newuserRoomDTO = new UserRoomKeyDto("testroom", "testUser", "token");
		roomService.addUserToRoom(newuserRoomDTO);
		Assert.assertTrue(roomService.usersInChatRoom("testroom", "testUser").isRight());
		Set<User> users = roomService.getUserList("testroom");
		Assert.assertTrue(users.size() ==1);
		
		UserRoomKeyDto newuserRoomDTO2 = new UserRoomKeyDto("testroom", "testUser2", "token2"); 
		roomService.addUserToRoom(newuserRoomDTO2);
		Assert.assertTrue(roomService.usersInChatRoom("testroom", "testUser2").isRight());
		users = roomService.getUserList("testroom");
		Assert.assertTrue(users.size() ==2);

		Either<AppError, ChatRoomUserListDto> usersList = roomService.removeUserFromRoom(newuserRoomDTO);
		Assert.assertFalse(usersList.contains(new ChatRoomUserListDto("testroom", HashSet.empty())));
	}

}
