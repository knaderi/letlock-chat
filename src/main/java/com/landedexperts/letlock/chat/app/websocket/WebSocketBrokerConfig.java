package com.landedexperts.letlock.chat.app.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

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
		// Set application destination prefixes. Client send messages at this end point
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
		registration.interceptors(new ChannelAutheticationInterceptor());
	}

}

