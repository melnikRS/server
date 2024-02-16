package ru.netology;

import org.apache.http.HttpStatus;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessRequest {
    public static void processRequest(Socket socket, ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers) {
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                responseWithoutContent(out, HttpStatus.SC_BAD_REQUEST, "Bad Request");
                return;
            }

            String method = parts[0];
            if (method == null || method.isBlank()) {
                responseWithoutContent(out, HttpStatus.SC_BAD_REQUEST, "Bad Request");
                return;
            }

            final  var path = parts[1];
            Request request = new Request(method, path);

            if (!handlers.containsKey(request.getMethod())) {
                responseWithoutContent(out, HttpStatus.SC_BAD_REQUEST, "Bad Request");
                return;
            }

            Map<String, Handler> handlerMap= handlers.get(request.getMethod());
            String requestPath = request.getPath();

            if (handlerMap.containsKey(requestPath)) {
                Handler handler = handlerMap.get(requestPath);
                handler.handle(request, out);
            } else {
                responseWithoutContent(out, HttpStatus.SC_NOT_FOUND, "Not Found");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void responseWithoutContent(BufferedOutputStream out, int responseCode, String responseStatus) throws IOException {
        out.write(("HTTP 1.1/ " + responseCode + " " + responseStatus + "\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n" +
                    "\r\n"
                ).getBytes());
        out.flush();
    }

}
