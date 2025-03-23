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
        app.get("/history/task/:id", this::getHistoryTask);
        app.get("/timeline/task/:id", this::getTimelineTask);
    }

    private void getHistoryTask(Context ctx) {
        var id = UUID.fromString(ctx.pathParam("id"));
        var data = historyService.getHistory(id);
        Response.ok(ctx, data);
    }

    private void getTimelineTask(Context ctx) {
        var id = UUID.fromString(ctx.pathParam("id"));
    }

}
