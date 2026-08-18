package me.mteach.dameng;

import com.intellij.database.Dbms;
import com.intellij.database.dataSource.DatabaseConnectionCore;
import com.intellij.database.dialects.base.introspector.jdbc.JdbcSourceLoader;
import com.intellij.database.model.DasObject;
import com.intellij.database.model.DasRoutine;
import com.intellij.database.model.DasTable;
import com.intellij.database.model.basic.BasicModSourceAware;
import com.intellij.database.util.DasUtil;
import com.intellij.database.util.DbImplUtilCore;
import com.intellij.openapi.util.Pair;

import java.sql.SQLException;

/**
 * Source (DDL) loader for the Dameng (DM) database.
 * <p>
 * The generic JDBC loader does not support view or routine definitions
 * ({@code supportsViewDefinition() == false}), so DM views show no DDL.
 * DM8 ships Oracle-compatible data dictionary views, so definitions can be
 * loaded from {@code ALL_VIEWS} / {@code ALL_SOURCE}.
 */
public class DmJdbcSourceLoader extends JdbcSourceLoader {

    public DmJdbcSourceLoader(Dbms dbms) {
        super(dbms);
    }

    @Override
    public void loadSource(BasicModSourceAware element, DatabaseConnectionCore connection) throws Exception {
        if (element != null) {
            try {
                if (element instanceof DasTable && loadDmView(element, connection)) return;
                if (element instanceof DasRoutine && loadDmRoutine(element, connection)) return;
            }
            catch (Exception e) {
                // Dictionary query failed (e.g. instance in a non-Oracle compatibility
                // mode without these views): degrade gracefully instead of failing.
            }
        }
        super.loadSource(element, connection);
    }

    private boolean loadDmView(BasicModSourceAware element, DatabaseConnectionCore connection) throws SQLException {
        String schema = JdbcSourceLoader.str(DasUtil.getSchema((DasObject) element));
        String name = JdbcSourceLoader.str(((DasObject) element).getName());
        String sql = "SELECT TEXT FROM SYS.ALL_VIEWS WHERE OWNER = " + schema + " AND VIEW_NAME = " + name;
        String ddl = DbImplUtilCore.concatStringResults(
                connection, connection.getDbms(), 0, sql, new StringBuilder(),
                DbImplUtilCore.ConcatenationProps.NO_CONCAT).toString();
        if (ddl.isEmpty()) return false;
        JdbcSourceLoader.applySourceText(element, ddl, false);
        return true;
    }

    private boolean loadDmRoutine(BasicModSourceAware element, DatabaseConnectionCore connection) throws SQLException {
        DasRoutine routine = (DasRoutine) element;
        String schema = JdbcSourceLoader.str(DasUtil.getSchema(routine));
        String name = JdbcSourceLoader.str(routine.getName());
        String type = routine.getRoutineKind() == DasRoutine.Kind.FUNCTION ? "FUNCTION" : "PROCEDURE";
        String sql = "SELECT TEXT FROM SYS.ALL_SOURCE WHERE OWNER = " + schema
                     + " AND NAME = " + name + " AND TYPE = " + JdbcSourceLoader.str(type) + " ORDER BY LINE";
        String ddl = DbImplUtilCore.concatStringResults(
                connection, connection.getDbms(), 0, sql, new StringBuilder(),
                DbImplUtilCore.ConcatenationProps.LINES).toString();
        if (ddl.isEmpty()) return false;
        JdbcSourceLoader.applySourceText(element, ddl, false);
        return true;
    }

    // DDL generators may call these directly (without a connection context);
    // provide the DM SQL for views, keep generic behaviour for everything else.

    @Override
    public boolean supportsViewDefinition() {
        return true;
    }

    @Override
    public boolean supportsProcedureDefinition() {
        return true;
    }

    @Override
    public Pair<String, DbImplUtilCore.ConcatenationProps> sqlViewDefinition(DasObject element) {
        String schema = JdbcSourceLoader.str(DasUtil.getSchema(element));
        String name = JdbcSourceLoader.str(element.getName());
        return Pair.create(
                "SELECT TEXT FROM SYS.ALL_VIEWS WHERE OWNER = " + schema + " AND VIEW_NAME = " + name,
                DbImplUtilCore.ConcatenationProps.NO_CONCAT);
    }

    @Override
    public Pair<String, DbImplUtilCore.ConcatenationProps> sqlProcedureDefinition(DasRoutine routine) {
        String schema = JdbcSourceLoader.str(DasUtil.getSchema(routine));
        String name = JdbcSourceLoader.str(routine.getName());
        String type = routine.getRoutineKind() == DasRoutine.Kind.FUNCTION ? "FUNCTION" : "PROCEDURE";
        return Pair.create(
                "SELECT TEXT FROM SYS.ALL_SOURCE WHERE OWNER = " + schema + " AND NAME = " + name
                + " AND TYPE = " + JdbcSourceLoader.str(type) + " ORDER BY LINE",
                DbImplUtilCore.ConcatenationProps.LINES);
    }
}
