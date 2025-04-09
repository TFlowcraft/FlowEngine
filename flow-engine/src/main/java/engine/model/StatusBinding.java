package engine.model;

import engine.common.Status;
import org.jetbrains.annotations.NotNull;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.SQLException;

public class StatusBinding implements Binding<String, Status> {

    @NotNull
    @Override
    public Converter<String, Status> converter() {
        return new Converter<>() {
            @Override
            public Status from(String databaseValue) {
                return databaseValue == null ? null : Status.valueOf(databaseValue);
            }

            @Override
            public String to(Status userObject) {
                return userObject == null ? null : userObject.name();
            }

            @NotNull
            @Override
            public Class<String> fromType() {
                return String.class;
            }

            @NotNull
            @Override
            public Class<Status> toType() {
                return Status.class;
            }
        };
    }

    @Override
    public void sql(BindingSQLContext<Status> ctx) {
        String value = converter().to(ctx.value());
        ctx.render().visit(DSL.val(value, SQLDataType.VARCHAR));
    }

    @Override
    public void register(BindingRegisterContext<Status> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), java.sql.Types.VARCHAR);
    }

    @Override
    public void set(BindingSetStatementContext<Status> ctx) throws SQLException {
        ctx.statement().setString(ctx.index(), converter().to(ctx.value()));
    }

    @Override
    public void get(BindingGetResultSetContext<Status> ctx) throws SQLException {
        String val = ctx.resultSet().getString(ctx.index());
        ctx.value(converter().from(val));
    }

    @Override
    public void get(BindingGetStatementContext<Status> ctx) throws SQLException {
        String val = ctx.statement().getString(ctx.index());
        ctx.value(converter().from(val));
    }

    @Override
    public void set(BindingSetSQLOutputContext<Status> ctx) throws SQLException {
        ctx.output().writeString(converter().to(ctx.value()));
    }

    @Override
    public void get(BindingGetSQLInputContext<Status> ctx) throws SQLException {
        String val = ctx.input().readString();
        ctx.value(converter().from(val));
    }
}
