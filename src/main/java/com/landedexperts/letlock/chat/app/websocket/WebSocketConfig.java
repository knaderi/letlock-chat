package com.landedexperts.letlock.chat.app.websocket;

import java.security.Principal;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/chat");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000",
                        "chrome-extension://ggnhohnkfcpcanfekomdkjffnfcjnjam")
                .withSockJS();
    }
    
	@Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(new MyChannelInterceptor());
    }
}

@SuppressWarnings("deprecation")
class MyChannelInterceptor extends ChannelInterceptorAdapter {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        accessor.getNativeHeader("token");
        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            Principal user = null ; // access authentication header(s) replace thsi w
            accessor.setUser(user);
        }
        return message;
    }
}