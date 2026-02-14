package com.example.shop.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.OffsetDateTime;

@Component
public class AccessLogFilter extends OncePerRequestFilter {

    private final Path logPath;

    public AccessLogFilter(@Value("${app.accesslog.path:/app/logs/access.log}") String logFilePath) {
        this.logPath = Paths.get(logFilePath);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        StatusCaptureResponse wrapped = new StatusCaptureResponse(response);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, wrapped);
        } finally {
            long tookMs = System.currentTimeMillis() - start;

            String ts = OffsetDateTime.now().toString();
            String method = request.getMethod();
            String path = request.getRequestURI();
            int status = wrapped.getStatus();

            String user = "anonymousUser";
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
                    user = auth.getName();
                }
            } catch (Exception ignored) {}

            String line = String.format("%s|%d|%s|%s|%d|%s%n",
                    ts, tookMs, method, path, status, user);

            writeLineSafely(line);
        }
    }

    private void writeLineSafely(String line) {
        try {
            Path parent = logPath.getParent();
            if (parent != null) Files.createDirectories(parent);

            Files.write(
                    logPath,
                    line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {}
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        return p.startsWith("/h2-console");
    }

    private static class StatusCaptureResponse extends HttpServletResponseWrapper {
        private int status = 200;

        StatusCaptureResponse(HttpServletResponse response) {
            super(response);
        }

        @Override public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }

        @Override public void sendError(int sc) throws IOException {
            this.status = sc;
            super.sendError(sc);
        }

        @Override public void sendError(int sc, String msg) throws IOException {
            this.status = sc;
            super.sendError(sc, msg);
        }

        @Override public void sendRedirect(String location) throws IOException {
            // redirect sets a 302 (or similar)
            this.status = getStatus();
            super.sendRedirect(location);
        }

        @Override
        public int getStatus() {
            return this.status;
        }
    }
}
