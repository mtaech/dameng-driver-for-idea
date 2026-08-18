package me.mteach.dameng;

import com.intellij.database.dataSource.DatabaseConnectionCore;
import com.intellij.database.dataSource.connection.Either;
import com.intellij.database.dataSource.connection.EitherKt;
import com.intellij.database.dataSource.connection.statements.ParameterizedSmartStatement;
import com.intellij.database.dataSource.connection.statements.ReusableSmartStatement;
import com.intellij.database.dataSource.connection.statements.SmartStatementFactoryService;
import com.intellij.database.dataSource.connection.statements.StandardResultsProcessors;
import com.intellij.database.dataSource.connection.statements.StatementParameters;
import com.intellij.database.dialects.AbstractDefinitionProvider;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.DasRoutine;
import com.intellij.database.model.ObjectKind;
import com.intellij.database.util.DasUtil;
import com.intellij.util.PairConsumer;

/**
 * Definition provider (Ctrl+Click / Go to DDL from the query console) for the
 * Dameng (DM) database. Generic data sources have no definition provider, so
 * navigating from a table name in the console does not open the DDL. DM8 ships
 * the Oracle-compatible {@code DBMS_METADATA.GET_DDL} package; we use it the
 * same way the Oracle dialect does.
 */
public class DmDefinitionProvider extends AbstractDefinitionProvider {

    private static final String GET_DDL_SQL = "SELECT DBMS_METADATA.GET_DDL(?, ?, ?) FROM DUAL";

    @Override
    public boolean isSupported(DasObject object) {
        ObjectKind kind = object.getKind();
        return kind == ObjectKind.TABLE
               || kind == ObjectKind.VIEW
               || kind == ObjectKind.ROUTINE
               || kind == ObjectKind.INDEX
               || kind == ObjectKind.SEQUENCE;
    }

    @Override
    protected void fetchSources(Iterable<? extends DasObject> objects,
                                DatabaseConnectionCore connection,
                                PairConsumer<DasObject, Object> consumer) throws Exception {
        ReusableSmartStatement statement = SmartStatementFactoryService.getInstance()
                .poweredBy(connection)
                .parameterized()
                .reuse(GET_DDL_SQL);
        try {
            for (DasObject object : objects) {
                StatementParameters parameters = new StatementParameters()
                        .text(getKindName(object))
                        .text(object.getName())
                        .text(DasUtil.getSchema(object));
                @SuppressWarnings("rawtypes")
                Either result = statement.execute(
                        parameters.asDecoration(), StandardResultsProcessors.FIRST_STRING);
                consumer.consume(object, EitherKt.any(result));
            }
        }
        finally {
            statement.close();
        }
    }

    private static String getKindName(DasObject object) {
        if (object.getKind() == ObjectKind.ROUTINE) {
            DasRoutine routine = (DasRoutine) object;
            return routine.getRoutineKind() == DasRoutine.Kind.FUNCTION ? "FUNCTION" : "PROCEDURE";
        }
        return object.getKind().name();
    }
}