package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, Map<String, Handler>> handlers;
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    public Server(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
        handlers = new ConcurrentHashMap<>();
    }

    public void startServer() {
        System.out.println("Server running...");
        try (final var serverSocket = new ServerSocket(9999)) {
            do {
                final var socket = serverSocket.accept();
                executorService.submit(() -> connection(socket));
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connection(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
            while (true) {
                final var requestLine = in.readLine();
                if (requestLine == null || requestLine.trim().length() == 0) {
                    break;
                }
                final var parts = requestLine.split(" ");

                if (parts.length != 3) {
                    socket.close();
                    break;
                }
                String method = parts[0];
                final var path = parts[1];
                Request request = createRequest(method, path);
                if (request == null || !handlers.containsKey(request.getMethod())) {
                    responseError(out, "404", "NOT FOUND");
                    break;
                }
                Map<String, Handler> map = handlers.get(request.getMethod());
                String requestPath = request.getPath();
                if (map.containsKey(requestPath)) {
                    Handler handler = map.get(requestPath);
                    handler.handle(request, out);
                } else {
                    if (!validPaths.contains(path)) {
                        responseError(out, "404", "NOT FOUND");
                    } else {
                        defaultLinkConnection(out, path);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Request createRequest(String method, String path) {
        return (method != null && !method.isBlank()) ? new Request(method, path) : null;
    }

    public void responseError(BufferedOutputStream out, String responseNumberError, String responseStatusError) {
        try {
            out.write((
                    "HTTP/1.1 " + responseNumberError + " " + responseStatusError + "\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void defaultLinkConnection(BufferedOutputStream out, String path) {
        try {
            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            // special case for classic
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (handlers.containsKey(method)) {
            handlers.get(method).put(path, handler);
        } else {
            handlers.put(method, new HashMap<>());
        }
    }
}
