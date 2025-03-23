package api;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public final class Response {
    private Response() {}

    public static <T> void ok(Context ctx, T data) {
        ctx.status(HttpStatus.OK).json(data);
    }

    public static <T> void created(Context ctx, T data) {
        ctx.status(HttpStatus.CREATED).json(data);
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
}
