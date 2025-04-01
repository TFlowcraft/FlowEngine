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
        app.get("/process/:processName/instance/:instanceId/task/history/:taskId", this::getInstanceHistoryTaskById);
        app.get("/process/:processName/instance/:instanceId/task/history/all", this::getInstanceHistoryTaskAll);
    }

    private void getInstanceHistoryTaskById(Context ctx) {
        var taskId = UUID.fromString(ctx.pathParam("id"));
        var processName = ctx.pathParam("processName");
        var instanceId = UUID.fromString(ctx.pathParam("instanceId"));

        var data = historyService.getHistoryTaskById(processName, instanceId, taskId);
        Response.ok(ctx, data);
    }

    private void getInstanceHistoryTaskAll(Context ctx) {
        var id = UUID.fromString(ctx.pathParam("id"));
        var processName = ctx.pathParam("processName");
        var instanceId = ctx.pathParam("instanceId");
    }


}
