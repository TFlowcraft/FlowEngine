package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;

public class EngineServerImpl implements RequestService {
    private static final int PORT = 8080;
    private static final Gson gson = new Gson();

    private static class RequestData {
        String processId;
        String instanceId;
    }

    @Override
    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        initContexts(server);
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server started on http://localhost:" + PORT);
    }

    private void initContexts(HttpServer server) {
        server.createContext("/start", this::handleStart);
        server.createContext("/complete", this::handleComplete);
        server.createContext("/status", this::handleStatus);
    }

    private void handleStart(HttpExchange exchange) throws IOException {
        if (!isMethodAllowed(exchange, Method.POST)) {
            return;
        }

        RequestData requestData = parseRequest(exchange);
        if (requestData == null || requestData.processId == null) {
            sendResponse(exchange, 400, ResponseEntity.error("Invalid request"));
            return;
        }

        String instanceId = "instance-" + requestData.processId;
        sendResponse(exchange, 200, ResponseEntity.success(Map.of("message", "Process started", "instanceId", instanceId)));
    }

    private void handleComplete(HttpExchange exchange) throws IOException {
        if (!isMethodAllowed(exchange, Method.POST)) {
            return;
        }

        RequestData requestData = parseRequest(exchange);
        if (requestData == null || requestData.instanceId == null) {
            sendResponse(exchange, 400, ResponseEntity.error("Invalid request"));
            return;
        }

        sendResponse(exchange, 200, ResponseEntity.success(Map.of("message", "Task completed", "instanceId", requestData.instanceId)));
    }

    private void handleStatus(HttpExchange exchange) throws IOException {
       if (!isMethodAllowed(exchange, Method.GET)) {
           return;
       }

        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("processId=")) {
            sendResponse(exchange, 400, ResponseEntity.error("Invalid query"));
            return;
        }

        String instanceId = query.split("=")[1];
        String status = "RUNNING";
        sendResponse(exchange, 200, ResponseEntity.success(Map.of("instanceId", instanceId, "status", status)));
    }

    private RequestData parseRequest(HttpExchange exchange) {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, RequestData.class);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isMethodAllowed(HttpExchange exchange, Method expectedMethod) throws IOException {
        if (!expectedMethod.toString().equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, ResponseEntity.error("Method Not Allowed"));
            return false;
        }
        return true;
    }


    private void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
