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

import static ru.netology.Request.GET;
import static ru.netology.Request.POST;

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
                if (request == null /*|| !handlers.containsKey(request.getMethod())*/) {
                    responseError(out, "404", "Not Found");
                } else {
                    printRequest(request);
                    responseGood(out);
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
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
    public void printRequest (Request request) {
        if (request.getMethod().equals(GET)) {
            System.out.println("МЕТОД: " + request.getMethod());
            System.out.println("ПУТЬ: " + request.getPath());
            System.out.println("ЗАГОЛОВОК: " + request.getHeaders());
            System.out.println("ПАРАМЕТРЫ {КЛЮЧ = ЗНАЧЕНИЕ}: ");
            for (var param: request.getQueryParams()) {
                System.out.println(param.getName() + "=" + param.getValue());
            }
        } else if (request.getMethod().equals(POST)) {
            System.out.println("МЕТОД: " + request.getMethod());
            System.out.println("ПУТЬ: " + request.getPath());
            System.out.println("ЗАГОЛОВОК: " + request.getHeaders());
            System.out.println("ПАРАМЕТРЫ {КЛЮЧ = ЗНАЧЕНИЕ}: ");
            System.out.println(request.getBody());
        }

//        System.out.println(request.getBody());
//        System.out.println(request.getQueryParam("АБОБА").getName());
//        System.out.println(request.getQueryParam("testDebugInfo").getValue());
    }


    public void addHandler(String method, String path, Handler handler) {
        if (handlers.containsKey(method)) {
            handlers.get(method).put(path, handler);
        } else {
            handlers.put(method, new HashMap<>());
        }
    }
}
