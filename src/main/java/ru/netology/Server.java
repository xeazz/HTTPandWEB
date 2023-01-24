package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, Map<String, Handler>> handlers;

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
        try (var in = new BufferedInputStream(socket.getInputStream());
             var out = new BufferedOutputStream(socket.getOutputStream())) {
            Request request = Request.createRequest(in);
            if (request == null || !handlers.containsKey(request.getMethod())) {
                responseError(out);
            } else {
                Map<String, Handler> map = handlers.get(request.getMethod());
                String requestPath = request.getFullPath();
                if (map.containsKey(requestPath)) {
                    Handler handler = map.get(requestPath);
                    handler.handle(request, out);
                    printRequest(request);
                } else {
                    if (map.containsKey(requestPath)) {
                        responseGood(out);
                    } else {
                        responseError(out);
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void responseError(BufferedOutputStream out) {
        try {
            out.write((
                    "HTTP/1.1 400 Bad Request\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void responseGood(BufferedOutputStream out) {
        try {
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printRequest(Request request) {
        System.out.println("METHOD: " + request.getMethod());
        System.out.println("PATH: " + request.getFullPath());
        System.out.println("HEADERS: " + request.getHeaders());
        System.out.println("QUERY PARAMETERS: ");
        if (request.getQueryParams() != null) {
            for (var param : request.getQueryParams()) {
                System.out.println("\t" + param.getName() + "=" + param.getValue());
            }
        } else {
            System.out.println("\tПараметры отсутствуют!");
        }

        System.out.println("BODY: ");
        if (request.getPostParams() != null) {
            for (var body : request.getPostParams()) {
                System.out.println("\t" + body.getName() + ": " + body.getValue());
            }
        } else {
            System.out.println("\tТело запроса отсутствует!");
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (handlers.containsKey(method)) {
            handlers.get(method).put(path, handler);
        } else {
            handlers.put(method, new HashMap<>());
            handlers.get(method).put(path, handler);
        }
    }
}


