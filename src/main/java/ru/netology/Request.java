package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Request {

    public static final String GET = "GET";
    public static final String POST = "POST";
    private final String method;
    private final String fullPath;
    private final List<String> headers;
    private final List<NameValuePair> queryParam;
    private final List<NameValuePair> body;


    public Request(String method, String fullPath, List<String> headers, List<NameValuePair> queryParam, List<NameValuePair> body) {
        this.method = method;
        this.fullPath = fullPath;
        this.headers = headers;
        this.queryParam = queryParam;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getFullPath() {
        return fullPath;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParam;
    }
    public List<NameValuePair> getPostParams() {
        return body;
    }
    public static Request createRequest(BufferedInputStream in) throws IOException, URISyntaxException {
        final var allowedMethods = List.of(GET, POST);
        // лимит на request line + заголовки
        final var limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            return null;
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return null;
        }

        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            return null;
        }

        final var fullPath = requestLine[1];
        if (!fullPath.startsWith("/")) {
            return null;
        }

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            return null;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        if (!method.equals(GET)) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers);
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);
                final var body = new String(bodyBytes);
                var requestBody = URLEncodedUtils.parse(body, StandardCharsets.UTF_8, '&');
                if (fullPath.contains("?")) {
                    var query = fullPath.substring(fullPath.indexOf("?")+ 1);
                    var queryParam = URLEncodedUtils.parse(query, StandardCharsets.UTF_8, '&');
                    return new Request(method, fullPath, headers, queryParam, requestBody);
                } else {
                    return new Request(method, fullPath, headers, null, requestBody);
                }
            }
        } else {
            if (fullPath.contains("?")) {
                var query = fullPath.substring(fullPath.indexOf("?") + 1);
                var queryParam = URLEncodedUtils.parse(query, StandardCharsets.UTF_8, '&');
                return new Request(method, fullPath, headers, queryParam, null);
            } else {
                return new Request(method, fullPath, headers, null, null);
            }
        }
        return null;
    }

    private static Optional<String> extractHeader(List<String> headers) {
        return headers.stream()
                .filter(o -> o.startsWith("Content-Length"))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}


