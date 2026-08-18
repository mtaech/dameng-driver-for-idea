package me.mteach.dameng;

import com.intellij.database.dataSource.DatabaseConnectionCore;
import com.intellij.database.dialects.base.introspector.jdbc.wrappers.ClosableIt;
import com.intellij.database.dialects.base.introspector.jdbc.wrappers.DatabaseMetaDataWrapper;
import com.intellij.database.dialects.generic.introspector.jdbc.GenericMetadataWrapper;
import com.intellij.database.remote.jdbc.RemoteDatabaseMetaData;
import com.intellij.util.containers.JBIterator;

import java.sql.SQLException;
import java.util.Collections;
import java.util.NoSuchElementException;

/**
 * Metadata wrapper for the Dameng (DM) JDBC driver.
 * <p>
 * DataGrip introspects foreign keys via {@code DatabaseMetaData.getImportedKeys()}.
 * The Dameng driver can exhaust the remote JDBC bridge process (OOM / SIGKILL)
 * when serving that call, which aborts the whole introspection session and leaves
 * the database tree empty. For DM we skip foreign-key introspection; tables,
 * columns, primary keys, indexes and routines are still introspected.
 */
public class DmMetadataWrapper extends GenericMetadataWrapper {

    public DmMetadataWrapper(DatabaseConnectionCore connection, RemoteDatabaseMetaData metaData) {
        super(connection, metaData);
    }

    @Override
    public ClosableIt.GroupingIt<TableFKey, TableFKeyColumn> tableFKeyColumns(Table table) throws SQLException {
        return new ClosableIt.GroupingIt<TableFKey, TableFKeyColumn>() {
            @Override
            public JBIterator<TableFKeyColumn> groupIt() {
                return JBIterator.from(Collections.emptyIterator());
            }

            @Override
            protected TableFKey nextImpl() {
                throw new NoSuchElementException();
            }

            @Override
            public void close() {
            }
        };
    }

    public static class Factory extends MDFactory {
        @Override
        public DatabaseMetaDataWrapper create(DatabaseConnectionCore connection, RemoteDatabaseMetaData metaData) {
            return new DmMetadataWrapper(connection, metaData);
        }
    }
}