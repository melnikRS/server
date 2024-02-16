package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ExecutorService executorService;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();
    private Socket socket;

    public Server(int port, int numberThreads) {
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(numberThreads);
    }

    public void start() {
        try (final var serverSocket = new ServerSocket(port)){
            System.out.println("Start server");
            socket = serverSocket.accept();
            executorService.submit(() -> ProcessRequest.processRequest(socket, handlers));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new ConcurrentHashMap<>());
        }
        handlers.get(method).put(path, handler);
    }

}