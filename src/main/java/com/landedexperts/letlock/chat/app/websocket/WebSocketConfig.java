package com.landedexperts.letlock.chat.app.websocket;

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

	/**
     * Prefix used by topics
     */
	private static final String topicPrefix = "/chat";

	/**
     * Prefix used for WebSocket broker destination mappings
     */
	private static final String applicatonPrefix = "/app";
	
	/**
     * Endpoint that can be used to connect to Websocket
     */
	private static final String endpoint = "/ws";

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		// Simple broker means a simple in-memory broker, and the destination prefix is
		// /chat.
		config.enableSimpleBroker(topicPrefix);
		//Set application destination prefixes. Client send messages at this end point
		config.setApplicationDestinationPrefixes(applicatonPrefix);
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint(endpoint)
				.setAllowedOrigins("http://localhost:3000", "chrome-extension://ggnhohnkfcpcanfekomdkjffnfcjnjam")
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
//            Principal user = null ; // access authentication header(s) replace this w
//            accessor.setUser(user);
		}
		return message;
	}
}