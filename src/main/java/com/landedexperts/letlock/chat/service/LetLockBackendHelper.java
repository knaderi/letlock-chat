package com.landedexperts.letlock.chat.service;

import java.net.HttpURLConnection;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.google.gson.Gson;
import com.landedexperts.letlock.chat.dto.BooleanResponse;
import com.landedexperts.letlock.chat.dto.SetResponse;


public class LetLockBackendHelper {
	private final Logger logger = LoggerFactory.getLogger(LetLockBackendHelper.class);
	
	private static LetLockBackendHelper instance = null;
	//private  String letLockBackendURI = "http://letlockbackenddev.us-west-2.elasticbeanstalk.com:5000";
	private  String letLockBackendURI = "http://localhost:5000";
	public static LetLockBackendHelper getInstance(String letLockBackendURI) {
		if(instance == null) {
			instance = new LetLockBackendHelper(letLockBackendURI);
		}
		return instance;
	}

    private LetLockBackendHelper(String letLockBackendURI) {
		this.letLockBackendURI = letLockBackendURI;
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

}
