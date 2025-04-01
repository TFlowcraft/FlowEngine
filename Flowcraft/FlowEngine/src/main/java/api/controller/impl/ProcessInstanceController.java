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
        app.get("/process/{processName}/instance/{id}", this::getProcessInstanceById);
        app.get("/process/{processName}/instance/all", this::getAllProcessInstances);
        app.get("/process/{processName}/diagram", this::getProcessDiagram);
    }

    private void getProcessInstanceById(Context ctx) {
        try {
            String processName = ctx.pathParam("processName");
            UUID id = UUID.fromString(ctx.pathParam("id"));

            var instance = processInstanceService.getProcessInstanceById(processName, id);
            Response.ok(ctx, instance);

        } catch (IllegalArgumentException e) {
            Response.handleValidationError(ctx, e);
        }
    }

    private void getAllProcessInstances(Context ctx) {
        try {
            String processName = ctx.pathParam("processName");
            var instances = processInstanceService.getAllProcessInstances(processName);
            Response.ok(ctx, instances);

        } catch (IllegalArgumentException e) {
            Response.handleValidationError(ctx, e);
        }
    }

    private void getProcessDiagram(Context ctx) {
        // Реализация получения диаграммы
    }
}