package com.landedexperts.letlock.chat.controller;

import static java.lang.String.format;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import com.google.gson.Gson;
import com.landedexperts.letlock.chat.domain.Room;
import com.landedexperts.letlock.chat.dto.ChatRoomUserListDto;
import com.landedexperts.letlock.chat.dto.NewRoomDto;
import com.landedexperts.letlock.chat.dto.SetResponse;
import com.landedexperts.letlock.chat.dto.SimpleRoomDto;
import com.landedexperts.letlock.chat.dto.UserRoomKeyDto;
import com.landedexperts.letlock.chat.message.Message;
import com.landedexperts.letlock.chat.message.MessageTypes;
import com.landedexperts.letlock.chat.service.RoomService;
import com.landedexperts.letlock.chat.user.User;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;

/**
* our chat front-end is subscribing to:
* '/chat/${username}/userList'
* '/chat/newRoom' - checked
* `/chat/${roomKey}/messages`
* /chat/${roomKey}/userList
* 
* It sends messages to:
* /app/chat/addRoom
* /app/chat/${roomKey}/join
* /app/chat/${roomKey}/leave
**/
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


	@SubscribeMapping("/chat/{userName}/roomList")
	public List<SimpleRoomDto> roomList(@DestinationVariable String userName) {
		return roomService.roomList(userName);
	}

	@MessageMapping("/chat/addRoom")
	@SendTo("/chat/newRoom")
	/**
	 * Convert messages that are headed to /app/addRoom and convert it to a new chatMessage
	 * and send it to "/chat/newRoom", so all subscribers  for "/chat/newRoom" will 
	 * receiver the message.
	 * @param newRoom
	 * @return
	 */
	public SimpleRoomDto addRoom(NewRoomDto newRoom) {
		// TODO: check if the token is valid and belongs to an authenticated user with a
		// file transfer uuid matching roomId
		// call backend, get the username for the token and check against file transfer
		// uuid. If it matches then allow adding room.
		return roomService.addRoom(newRoom.roomName); //returns where subscribed for the SimpleRoomDto is false. It doesn oty reurn Room as it does not need the list of users.
	}
	

	@MessageMapping("/chat/{roomId}/join")
	public ChatRoomUserListDto userJoinRoom(UserRoomKeyDto userRoomKey, SimpMessageHeaderAccessor headerAccessor) {
		// TODO: check if the token, username and file transferuuid are valid and then
		// allow the rest of the call goahead
		// adding the user to room. Update the has in roomService with roomId and token

//        with enabled spring security
//        final String securityUser = headerAccessor.getUser().getName();
		 System.out.println("authetication token at join is" + userRoomKey.token);
		if(!roomService.roomExist(userRoomKey)) {
			roomService.addRoom(userRoomKey.roomKey);
		}
		final String username = (String) headerAccessor.getSessionAttributes().put("username", userRoomKey.userName);
		final Message joinMessage = new Message(MessageTypes.JOIN, userRoomKey.userName, "user joined", userRoomKey.token);
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
	//TODO: shouldn't we have @DestinationVariable String roomId to use it.
	public ChatRoomUserListDto userLeaveRoom(UserRoomKeyDto userRoomKeyDto, SimpMessageHeaderAccessor headerAccessor) {
		// TODO: check if the token, username and file transferuuid are valid and then
		// allow the rest of the call remove the user from the room
        System.out.println("authetication token is" + userRoomKeyDto.token);
		final Message leaveMessage = new Message(MessageTypes.LEAVE, userRoomKeyDto.userName, "user left", userRoomKeyDto.token);
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
		// TODO: check if the token, username and file transferuuid are valid and then
		// allow the rest of the call goahead
		// adding the user to room. Update the has in roomService with roomId and token

//        with enabled spring security
//        final String securityUser = headerAccessor.getUser().getName();

		return roomService.getUserList(roomId);
	}



	@MessageMapping("/chat/{roomId}/sendMessage")
	public Message sendMessage(@DestinationVariable String roomId, Message message) {
		//TODO: check the map in roomservice to see if the token for the roomId exist, then allow continuing
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
		//Remove user's empty rooms
		userRooms.toStream()
	    .filter(room -> room.users.isEmpty())
	    .forEach(emptyRoom -> roomService.removeRoom(emptyRoom.name));
	}
	
    public java.util.Set<String> getUserChatRooms()
            throws Exception, UnsupportedEncodingException {
    	
    	Client client = ClientBuilder.newClient();

        WebTarget target = client.target("http://localhost:5000/get_user_chatroom_names")
           .queryParam("token", "asdasdasdasd")
           .queryParam("mode", "json");
        
        Response response =  target.request().accept(MediaType.APPLICATION_JSON_VALUE).get();
        if(response.getStatus() != HttpURLConnection.HTTP_OK) {
        	
        }else {
        	String replyString = response.readEntity(String.class);
        }

        String replyString = response.readEntity(String.class);
        SetResponse<String> responseObject = new Gson().fromJson(replyString, SetResponse.class);
        return responseObject.getResult();
    	
    }

}
