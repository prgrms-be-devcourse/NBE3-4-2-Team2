package com.example.backend.global.config.init;

// @Profile("dev") // dev 환경에서만 실행, FileUtil, CmdUtil은 dev 환경에서만 사용
// @Configuration
// public class ApiSchema {
// 	@Bean
// 	public ApplicationRunner devInitDataApplicationRunner() {
// 		return args -> {
// 			FileUtil.downloadByHttp("http://localhost:8080/v3/api-docs/apiV1", ".");
//
// 			String cmd = "yes | npx --package typescript --package openapi-typescript openapi-typescript apiV1.json -o ../frontend/src/lib/backend/apiV1/schema.d.ts";
// 			CmdUtil.runAsync(cmd);
// 		};
// 	}
// }
