package com.landedexperts.letlock.chat.app.websocket;

import org.springframework.messaging.MessagingException;

public class WebsocketConnectionAuthenticationException extends MessagingException {

	public WebsocketConnectionAuthenticationException(String description, Throwable cause) {
		super(description, cause);
	}

	public WebsocketConnectionAuthenticationException(String description) {
		super(description);
	}

	private static final long serialVersionUID = 4213513832227901794L;
	private static final String AUTHETICATION_TOKEN_MISSING_MSG = "{\"errorCode\": 404, \"errorMessage\":\"Authetication token missing\"}";

	public WebsocketConnectionAuthenticationException() {
		super(AUTHETICATION_TOKEN_MISSING_MSG);
	}
}