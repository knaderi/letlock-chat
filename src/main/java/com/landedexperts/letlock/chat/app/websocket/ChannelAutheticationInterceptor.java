package com.landedexperts.letlock.chat.app.websocket;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.landedexperts.letlock.chat.service.LetLockBackendHelper;

@Component
public class ChannelAutheticationInterceptor implements ChannelInterceptor {

//	@Value("${letlock.filetransfer.backend.login.url}")
	//private String letlockBackendURI = "http://letlockbackenddev.us-west-2.elasticbeanstalk.com:5000";
	private  String letLockBackendURI = "http://localhost:5000";
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		List<String> nativeHeader = accessor.getNativeHeader("token");
		String token = nativeHeader != null ? nativeHeader.get(0) : "MISSING_TOKEN";
		StompCommand command = accessor.getCommand();
		if (StompCommand.CONNECT.equals(command)) {
			if (!LetLockBackendHelper.getInstance(letLockBackendURI).authenticate(token)) {
				throw new RuntimeException("Authetication failed");
			}

		}
		return message;
	}

}
