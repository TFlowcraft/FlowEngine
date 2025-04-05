package api.controller.impl;

import api.Response;
import api.controller.ControllerSetup;
import api.service.TaskService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.UUID;

public class TaskController implements ControllerSetup {
    TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void registerEndpoints(Javalin app) {
        app.get("/process/{processName}/instance/{instanceId}/task", this::getAllInstanceTasks);
    }

    private void getAllInstanceTasks(Context ctx) {
        try {
            String processName = ctx.pathParam("processName");
            UUID instanceId = UUID.fromString(ctx.pathParam("instanceId"));
            var tasks = taskService.getAllTasksByInstanceId(processName, instanceId);
            Response.ok(ctx, tasks);
        } catch (IllegalArgumentException e) {
            Response.handleValidationError(ctx, e);
        }
    }
}
