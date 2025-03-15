package api;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ResponseEntity {
    private static final Gson gson = new Gson();

    public static String success(Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);
        return gson.toJson(response);
    }

    public static String error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return gson.toJson(response);
    }
}
