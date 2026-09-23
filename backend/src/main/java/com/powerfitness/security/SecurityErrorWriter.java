package com.powerfitness.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.MediaType;


final class SecurityErrorWriter {

    private SecurityErrorWriter() {}

    static void write(HttpServletResponse response, int status, String code, String message, String path)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String body = """
                {"timestamp":"%s","status":%d,"code":"%s","message":"%s","path":"%s","fieldErrors":[]}"""
                .formatted(Instant.now(), status, esc(code), esc(message), esc(path));
        response.getWriter().write(body);
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
