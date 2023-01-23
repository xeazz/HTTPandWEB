package ru.netology;

public class Main {
    public static void main(String[] args) {
        var server = new Server(64);
        server.addHandler("GET", "/messages", ((request, responseStream) ->
                server.responseError(responseStream)));
        server.addHandler("POST", "/messages", (request, responseStream) ->
                server.responseError(responseStream));
        server.addHandler("GET", "/classic.html", (request, outputStream) ->
                server.defaultLinkConnection(outputStream, "/classic.html"));
        server.addHandler("GET", "/spring.svg", (request, outputStream) ->
                server.defaultLinkConnection(outputStream, "/spring.svg"));

        server.startServer();
    }
}


