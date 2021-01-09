package com.landedexperts.letlock.chat.service;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import com.google.gson.Gson;
import com.landedexperts.letlock.chat.dto.BooleanResponse;
import com.landedexperts.letlock.chat.dto.SetResponse;

public class LetLockBackendServiceFacade {
	private final Logger logger = LoggerFactory.getLogger(LetLockBackendServiceFacade.class);

	@Value("${letlock.filetransfer.backend.login.url}")
	private String letLockBackendURI;

	public LetLockBackendServiceFacade(String msg) {
		logger.info(msg);
	}

	public Boolean authenticate(String token) {

		Client client = ClientBuilder.newClient();

		WebTarget target = client.target(letLockBackendURI + "/authenticate").queryParam("token", token)
				.queryParam("mode", "json");

		Response response = target.request().accept(MediaType.APPLICATION_JSON_VALUE).get();
		if (response.getStatus() == HttpURLConnection.HTTP_OK) {
			String replyString = response.readEntity(String.class);
			BooleanResponse responseObject = new Gson().fromJson(replyString, BooleanResponse.class);
			return responseObject.getResult().getValue();
		} else {
			logger.error("Autentication connection exception. Http Code: " + response.getStatus());
			return false;
		}
	}

	public Boolean authenticateForRoom(String token, String roomKey) {

		Client client = ClientBuilder.newClient();

		WebTarget target = client.target(letLockBackendURI + "/authenticate_for_chat_room").queryParam("token", token)
				.queryParam("roomKey", roomKey);

		Response response = target.request().accept(MediaType.APPLICATION_JSON_VALUE).get();
		if (response.getStatus() == HttpURLConnection.HTTP_OK) {
			String replyString = response.readEntity(String.class);
			BooleanResponse responseObject = new Gson().fromJson(replyString, BooleanResponse.class);
			return responseObject.getResult().getValue();
		} else {
			logger.error("Autentication connection exception. Http Code: " + response.getStatus());
			return false;
		}
	}
	
	public Set<String> getUserRooms(String token) {

		Client client = ClientBuilder.newClient();

		WebTarget target = client.target(letLockBackendURI + "/get_user_rooms").queryParam("token", token);

		Response response = target.request().accept(MediaType.APPLICATION_JSON_VALUE).get();
		if (response.getStatus() == HttpURLConnection.HTTP_OK) {
			String replyString = response.readEntity(String.class);
			SetResponse responseObject = new Gson().fromJson(replyString, SetResponse.class);
			return responseObject.getResult();
		} else {
			logger.error("Autentication connection exception. Http Code: " + response.getStatus());
			return Collections.EMPTY_SET;
		}
	}

}
