package ru.netology;

public class Request {

    private final String method;
    private final String path;


    public Request(String requestMethod, String requestPath) {
        this.method = requestMethod;
        this.path = requestPath;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

}