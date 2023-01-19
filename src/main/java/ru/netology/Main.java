package ru.netology;

public class Main {
    public static void main(String[] args) {
        var server = new Server(64);
        server.addHandler("GET", "/messages", ((request, responseStream) ->
                server.responseError(responseStream, "404", "Not Found")));
        server.addHandler("POST", "/messages", (request, responseStream) ->
                server.responseError(responseStream, "500", "Internal Server Error"));
        server.addHandler("GET", "/", (request, outputStream) ->
                server.defaultLinkConnection(outputStream, "index.html"));
        server.startServer();
    }
}


