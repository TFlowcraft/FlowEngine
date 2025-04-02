package api;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.jooq.XML;

public final class Response {
    private Response() {}

    public static <T> void ok(Context ctx, T data) {
        if (data instanceof XML xml) {
            ctx.status(HttpStatus.OK)
                    .contentType("application/xml")
                    .result(xml.data());
        } else {
            ctx.status(HttpStatus.OK).json(data);
        }
    }

    public static <T> void created(Context ctx, T data) {
        if (data instanceof XML xml) {
            ctx.status(HttpStatus.CREATED)
                    .contentType("application/xml")
                    .result(xml.data());
        } else {
            ctx.status(HttpStatus.CREATED).json(data);
        }
    }

    public static void noContent(Context ctx) {
        ctx.status(HttpStatus.NO_CONTENT);
    }

    public static void notFound(Context ctx) {
        ctx.status(HttpStatus.NOT_FOUND);
    }

    public static void internalServerError(Context ctx, String message) {
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(message);
    }

    public static void handleValidationError(Context ctx, Exception e) {
        if (e.getMessage().contains("not found")) {
            notFound(ctx);
        } else if (e.getMessage().contains("Invalid")) {
            Response.internalServerError(ctx, e.getMessage());
        } else {
            internalServerError(ctx, e.getMessage());
        }
    }

}
