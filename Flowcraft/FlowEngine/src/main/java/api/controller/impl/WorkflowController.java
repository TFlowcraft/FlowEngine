package api.controller.impl;

import api.controller.ControllerSetup;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class WorkflowController implements ControllerSetup {
    @Override
    public void registerEndpoints(Javalin app) {
        app.get("/process/{processName}/diagram", this::getWorkflow);
    }

    private void getWorkflow(Context ctx) {
        Object workflow = new Object();
        ctx.json(workflow);
    }


}
