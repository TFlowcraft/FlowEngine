package api.controller;

import io.javalin.Javalin;

public interface ControllerSetup {
    void registerEndpoints(Javalin app);
}
