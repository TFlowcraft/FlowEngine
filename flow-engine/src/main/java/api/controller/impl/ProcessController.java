package api.controller.impl;

import api.Response;
import api.controller.ControllerSetup;
import api.service.ProcessInfoService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class ProcessController implements ControllerSetup {
    private final ProcessInfoService processInfoService;

    public ProcessController(ProcessInfoService processInfoService) {
        this.processInfoService = processInfoService;
    }

    @Override
    public void registerEndpoints(Javalin app) {
        app.get("/process/info", this::getProcess);
    }

    public void getProcess(Context ctx) {
        var processes = processInfoService.getAllProcessesInfo();
        Response.ok(ctx, processes);
    }
}
