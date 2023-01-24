package ru.netology;

public class Main {
    public static void main(String[] args) {
        var server = new Server(64);
        server.addHandler("GET", "/messages", ((request, responseStream) ->
                server.responseGood(responseStream)));
        server.addHandler("GET", "/messages?last=10", ((request, responseStream) ->
                server.responseGood(responseStream)));
        server.addHandler("POST", "/classic.html/?value=pair", ((request, responseStream) ->
                server.responseGood(responseStream)));
        server.addHandler("POST", "/index.html", ((request, responseStream) ->
                server.responseGood(responseStream)));
        server.startServer();
    }
}


