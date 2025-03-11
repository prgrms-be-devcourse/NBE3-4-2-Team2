package com.example.backend.global.util;

import java.util.concurrent.TimeUnit;

public class CmdUtil {
	public static void runAsync(String cmd) {
		new Thread(() -> {
			run(cmd);
		}).start();
	}

	public static void run(String cmd) {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", cmd);
			Process process = processBuilder.start();
			process.waitFor(1, TimeUnit.MINUTES);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
