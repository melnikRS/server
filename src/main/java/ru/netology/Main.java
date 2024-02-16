package ru.netology;

import org.apache.http.HttpStatus;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
  public static int NUMBER_THREADS = 64;
  public static int PORT = 9999;

  public static void main(String[] args) {
    final var server = new Server(PORT, NUMBER_THREADS);

    server.addHandler("GET", "/messages",((request, responseStream) ->
            ProcessRequest.responseWithoutContent(responseStream, HttpStatus.SC_NOT_FOUND, "Not found")));

    server.addHandler("POST", "/messages",((request, responseStream) ->
            defaultHandler(responseStream, "spring.svg")));

    server.addHandler("GET", "/",((request, responseStream) ->
            defaultHandler(responseStream, "spring.svg")));

    server.start();

  }

  static void defaultHandler(BufferedOutputStream out, String path) throws IOException {
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
      return;
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
  }

}


