package com.landedexperts.letlock.chat.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SetResponse<T> {
	@JsonProperty
	protected String returnCode = "SUCCESS";
	
	@JsonProperty
	protected String returnMessage = "";
	
	@JsonProperty
	private Set<T> result;

	public String getReturnCode() {
		return this.returnCode;
	}

	public Set<T> getResult() {
		return result;
	}

	public SetResponse(final Set<T> resultValue, final String returnCode, final String returnMessage) {
		this.returnCode = returnCode != null ? returnCode : "";
		this.returnMessage = returnMessage != null ? returnMessage : "";
		this.result = resultValue;
	}
}
