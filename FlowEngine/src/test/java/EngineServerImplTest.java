
import api.EngineServerImpl;
import api.ResponseEntity;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EngineServerImplTest {
    public EngineServerImpl server;
    public HttpExchange exchange;
    public static final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        server = new EngineServerImpl();
        exchange = mock(HttpExchange.class);
    }

    /**
     * Тест обработки /start (POST)
     */
    @Test
    void testHandleStart_Success() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String requestBody = gson.toJson(Map.of("processId", "12345"));
        setExchangeMock("/start", "POST", requestBody);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(outputStream);

        server.getClass().getDeclaredMethod("handleStart", HttpExchange.class).setAccessible(true);
        server.getClass().getDeclaredMethod("handleStart", HttpExchange.class)
                .invoke(server, exchange);

        String expectedResponse = ResponseEntity.success(Map.of("message", "Process started", "instanceId", "instance-12345"));
        assertEquals(expectedResponse, outputStream.toString(StandardCharsets.UTF_8));
    }

    /**
     * Тест обработки /complete (POST)
     */
    @Test
    void testHandleComplete_Success() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String requestBody = gson.toJson(Map.of("instanceId", "instance-12345"));
        setExchangeMock("/complete", "POST", requestBody);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(outputStream);

        server.getClass().getDeclaredMethod("handleComplete", HttpExchange.class).setAccessible(true);
        server.getClass().getDeclaredMethod("handleComplete", HttpExchange.class)
                .invoke(server, exchange);

        String expectedResponse = ResponseEntity.success(Map.of("message", "Task completed", "instanceId", "instance-12345"));
        assertEquals(expectedResponse, outputStream.toString(StandardCharsets.UTF_8));
    }

    /**
     * Тест обработки /status (GET)
     */
    @Test
    void testHandleStatus_Success() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(URI.create("/status?processId=12345"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(outputStream);

        server.getClass().getDeclaredMethod("handleStatus", HttpExchange.class).setAccessible(true);
        server.getClass().getDeclaredMethod("handleStatus", HttpExchange.class)
                .invoke(server, exchange);

        String expectedResponse = ResponseEntity.success(Map.of("instanceId", "12345", "status", "RUNNING"));
        assertEquals(expectedResponse, outputStream.toString(StandardCharsets.UTF_8));
    }

    /**
     * Тест ошибки: неверный метод запроса
     */
    @Test
    void testHandleStart_MethodNotAllowed() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        setExchangeMock("/start", "GET", "");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(outputStream);

        server.getClass().getDeclaredMethod("handleStart", HttpExchange.class).setAccessible(true);
        server.getClass().getDeclaredMethod("handleStart", HttpExchange.class)
                .invoke(server, exchange);

        String expectedResponse = ResponseEntity.error("Method Not Allowed");
        assertEquals(expectedResponse, outputStream.toString(StandardCharsets.UTF_8));
    }

    /**
     * Вспомогательный метод для установки mock HttpExchange
     */
    private void setExchangeMock(String path, String method, String requestBody) throws IOException {
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(URI.create(path));

        InputStream inputStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(inputStream);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(outputStream);
    }
}
