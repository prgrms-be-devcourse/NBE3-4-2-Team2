package com.example.backend.global.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.SneakyThrows;

public class JsonUtil {
	private static final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule()).setSerializationInclusion(
		JsonInclude.Include.NON_NULL);

	@SneakyThrows
	public static String toString(Object obj) {
		return om.writeValueAsString(obj);
	}
}
