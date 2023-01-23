package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        var server = new Server(64);
//        server.addHandler("GET", "/messages", ((request, responseStream) ->
//                server.responseError(responseStream, "404", "Not Found")));
//        server.addHandler("POST", "/messages", (request, responseStream) ->
//                server.responseError(responseStream, "500", "Internal Server Error"));
//        server.addHandler("GET", "/classic.html", (request, outputStream) ->
//                server.defaultLinkConnection(outputStream, "/classic.html"));
//        server.addHandler("GET", "/", new Handler() {
//            @Override
//            public void handle(Request request, BufferedOutputStream outputStream) throws IOException {
//                String content = "GG/BB";
//                final var mimeType = "text/plane";
//                final var length = content.length();
//                outputStream.write((
//                        "HTTP/1.1 200 OK\r\n" +
//                                "Content-Type: " + mimeType + "\r\n" +
//                                "Content-Length: " + length + "\r\n" +
//                                "Connection: close\r\n" +
//                                "\r\n"
//                ).getBytes());
//                outputStream.flush();
//            }
//        });
        server.startServer();
    }
}


