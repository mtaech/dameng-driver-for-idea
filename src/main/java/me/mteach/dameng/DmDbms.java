package me.mteach.dameng;

import com.intellij.database.Dbms;
import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/**
 * Holds the {@link Dbms} instance for Dameng (DM). Registered in plugin.xml via
 * {@code <dbms id="DM" instance="me.mteach.dameng.DmDbms.DM"/>}; the predef
 * product-name pattern matches "DM DBMS" so DM connections are detected as this
 * DBMS instead of falling back to UNKNOWN (same approach as the built-in
 * {@code GenericDbms} class).
 */
public final class DmDbms {

    public static final Dbms DM = Dbms.create(
            "DAMENG",
            "达梦数据库 (DM)",
            icon(),
            "dm|dameng");

    private DmDbms() {
    }

    private static java.util.function.Supplier<Icon> icon() {
        return () -> IconLoader.getIcon("/icons/dm.svg", DmDbms.class.getClassLoader());
    }
}