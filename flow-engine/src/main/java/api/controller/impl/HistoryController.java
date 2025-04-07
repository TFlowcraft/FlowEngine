package api.controller.impl;

import api.Response;
import api.controller.ControllerSetup;
import api.service.HistoryService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.UUID;

public class HistoryController implements ControllerSetup {
    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }


    @Override
    public void registerEndpoints(Javalin app) {
        app.get("/process/{processName}/instance/{instanceId}/task/history/{taskId}", this::getInstanceHistoryTaskById);
        app.get("/process/{processName}/instance/{instanceId}/task/history", this::getInstanceHistoryTaskAll);
    }

    private void getInstanceHistoryTaskById(Context ctx) {
        try {
            System.out.println(ctx.pathParam("taskId"));
            System.out.println(ctx.pathParam("processName"));
            System.out.println(ctx.pathParam("instanceId"));
            var taskId = UUID.fromString(ctx.pathParam("taskId"));
            var processName = ctx.pathParam("processName");
            var instanceId = UUID.fromString(ctx.pathParam("instanceId"));

            var historyTask = historyService.getHistoryTaskById(processName, instanceId, taskId);
            Response.ok(ctx, historyTask);

        } catch (IllegalArgumentException e) {
            Response.handleValidationError(ctx, e);
        }
    }

    private void getInstanceHistoryTaskAll(Context ctx) {
        try {
            var processName = ctx.pathParam("processName");
            var instanceId = UUID.fromString(ctx.pathParam("instanceId"));
            var historyTasks = historyService.getAllHistoryTask(processName, instanceId);
            Response.ok(ctx, historyTasks);

        } catch (IllegalArgumentException e) {
            Response.handleValidationError(ctx, e);
        }
    }


}
