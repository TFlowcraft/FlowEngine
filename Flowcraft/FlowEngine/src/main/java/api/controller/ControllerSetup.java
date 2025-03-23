package api.controller;

import io.javalin.Javalin;

public interface ControllerEndpoinsSetup {
    void registerEndpoints(Javalin app);
}
