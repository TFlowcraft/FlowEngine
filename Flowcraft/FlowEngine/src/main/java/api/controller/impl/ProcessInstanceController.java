package api.controller.impl;

import api.Response;
import api.controller.ControllerSetup;
import api.service.ProcessInfoService;
import api.service.ProcessInstanceService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.eclipse.jetty.xml.XmlParser;
import org.jooq.XML;

import java.util.UUID;

public class ProcessInstanceController implements ControllerSetup {
    private final ProcessInstanceService processInstanceService;
    private final ProcessInfoService processInfoService;

    public ProcessInstanceController(ProcessInstanceService processInstanceService, ProcessInfoService processInfoService) {
        this.processInstanceService = processInstanceService;
        this.processInfoService = processInfoService;
    }

    @Override
    public void registerEndpoints(Javalin app) {
        app.get("/", this::infoEndpoint);
        app.get("/process/{processName}/instance/{id}", this::getProcessInstanceById);
        app.get("/process/{processName}/instance", this::getAllProcessInstances);
        app.get("/process/{processName}/diagram", this::getProcessDiagram);
    }

    private void infoEndpoint(Context ctx) {
       String info = "Javalin web app";
       Response.ok(ctx, info);
    }

    private void getProcessInstanceById(Context ctx) {
        try {
            String processName = ctx.pathParam("processName");
            var sId = ctx.pathParam("id");
            System.out.println(sId);
            UUID id = UUID.fromString(sId);

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
            e.printStackTrace();
        }
    }

    private void getProcessDiagram(Context ctx) {
       try {
           String processName = ctx.pathParam("processName");
           var file = processInfoService.getBpmnFile(processName);
           Response.ok(ctx, file);
       } catch (IllegalArgumentException e) {
           Response.handleValidationError(ctx, e);
           e.printStackTrace();
       }
    }
}