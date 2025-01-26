package com.example.backend.global.rs;

import lombok.Builder;

@Builder
public record ErrorRs(String target, Integer code, String message) {

}
