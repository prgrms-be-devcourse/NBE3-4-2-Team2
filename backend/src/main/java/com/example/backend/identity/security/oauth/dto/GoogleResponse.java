package com.example.backend.identity.security.oauth.dto;

import java.util.Map;

public class GoogleResponse implements OAuth2Response {

	private final Map<String, Object> attribute;
	private final String phoneNumber;

	public GoogleResponse(Map<String, Object> attribute, String phoneNumber) {
		this.attribute = attribute;
		this.phoneNumber = phoneNumber;
	}

	@Override
	public String getProvider() {

		return "google";
	}

	@Override
	public String getProviderId() {

		return attribute.get("sub").toString();
	}

	@Override
	public String getEmail() {

		return attribute.get("email").toString();
	}

	@Override
	public String getName() {

		return attribute.get("name").toString();
	}

	@Override
	public String getPhoneNumber() {
		return phoneNumber;
	}
}
