package api.controller.impl;

import api.Response;
import api.controller.ControllerSetup;
import api.service.ProcessService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class ProcessController implements ControllerSetup {
    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    @Override
    public void registerEndpoints(Javalin app) {
        app.get("/process/info", this::getProcess);
    }

    public void getProcess(Context ctx) {
        var processes = processService.getAllProcessInfos();
        Response.ok(ctx, processes);
    }
}
