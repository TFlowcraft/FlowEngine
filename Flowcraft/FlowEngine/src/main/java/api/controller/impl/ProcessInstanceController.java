package api.controller.impl;

import api.Response;
import api.controller.ControllerSetup;
import api.service.ProcessInstanceService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.UUID;

public class ProcessInstanceController implements ControllerSetup {
    private final ProcessInstanceService processInstanceService;

    public ProcessInstanceController(ProcessInstanceService processInstanceService) {
        this.processInstanceService = processInstanceService;
    }

    @Override
    public void registerEndpoints(Javalin app) {
        app.get("/processInstance/single/:id", this::getSingleProcessInstance);
        app.get("/processInstance/all", this::getAllProcessInstances);
    }

    private void getSingleProcessInstance(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        var instance = processInstanceService.getProcessInstance(id);
        if (instance == null) {
            Response.notFound(ctx);
        } else {
            Response.ok(ctx, instance);
        }
    }

    private void getAllProcessInstances(Context ctx) {
        var instances = processInstanceService.getProcessInstances();
        Response.ok(ctx, instances);
    }
}
