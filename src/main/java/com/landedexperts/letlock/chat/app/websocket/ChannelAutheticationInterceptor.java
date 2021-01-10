package com.landedexperts.letlock.chat.app.websocket;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import com.landedexperts.letlock.chat.service.LetLockBackendServiceFacade;


@SpringBootConfiguration
public class ChannelAutheticationInterceptor implements ChannelInterceptor, ApplicationContextAware {

	@Autowired
	private static ApplicationContext applicationContext;
	
	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		List<String> nativeHeader = accessor.getNativeHeader("token");
		String token = nativeHeader != null ? nativeHeader.get(0) : "MISSING_TOKEN";
		StompCommand command = accessor.getCommand();
		if (StompCommand.CONNECT.equals(command)) {
			if (!applicationContext.getBean(LetLockBackendServiceFacade.class).validateToken(token)) {
				throw new RuntimeException("Failed calling File Transfer Backend to validate token.");
			}

		}
		return message;
	}
	
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
    
    public static ApplicationContext getCtx() {
        return applicationContext;
    }

}
