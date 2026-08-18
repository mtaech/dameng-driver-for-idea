# dameng-driver-for-idea

[达梦数据库 (Dameng, DM)](https://www.dameng.com) support for **JetBrains IDEs** —
DataGrip, IntelliJ IDEA Ultimate, and any IDE with the *Database Tools and SQL* plugin
(`com.intellij.database`).

## What it does

| Feature | Details |
|---|---|
| Driver registration | `DM (达梦数据库)` for DM8, `DM7 (达梦数据库 7)` for DM7 — both with a dedicated icon |
| Driver jar | DM8: `DmJdbcDriver18` 8.1.3.140; DM7: `Dm7JdbcDriver17` 7.6.0.165 — downloaded from Maven Central automatically |
| URL template | `jdbc:dm://host:5236`, optional `schema=…&compatibleMode=oracle\|mysql…` query params |
| Default port | 5236 |
| Keep-alive | `SELECT 1 FROM DUAL` |
| View DDL | Loaded from `SYS.ALL_VIEWS` |
| Procedure / function DDL | Loaded from `SYS.ALL_SOURCE` |
| Console navigation | Ctrl+Click / Go to DDL on a table name opens its DDL via `DBMS_METADATA.GET_DDL` |
| Foreign keys | Introspection skipped — see [Known limitations](#known-limitations) |

## Install

1. Build: `./gradlew buildPlugin` → `build/distributions/dameng-driver-for-idea-<version>.zip`
2. IDE → Settings → Plugins → ⚙ → *Install Plugin from Disk…*

Requires an IDE with the *Database Tools and SQL* plugin (DataGrip, IDEA Ultimate,
PyCharm Pro, etc.). IDEA Community is not supported.

## Use

1. Database tool window → **+** → **Driver** → **DM (达梦数据库)** (or create a data
   source and pick the DM driver).
2. Fill host/port (default `5236`), user, password. Set the current schema via the
   `schema` URL parameter if needed.
3. Test Connection — the driver jar is downloaded on first use.
4. In the data source **Schemas** tab, check the schemas you want to introspect.

## Known limitations

- **DM7**: supported via the separate `DM7 (达梦数据库 7)` driver (Dm7JdbcDriver17),
  same URL/dialect conventions. The DM7 data dictionary is older — `SYS.ALL_VIEWS` /
  `SYS.ALL_SOURCE` and `DBMS_METADATA.GET_DDL` are present in Oracle-compatibility
  mode but were not verified against a live DM7 instance (only DM8 was tested). If
  any dictionary call fails, DDL loading degrades gracefully instead of breaking.
- **Foreign keys are not introspected.** The Dameng JDBC driver can exhaust the remote
  JDBC bridge process (OOM / SIGKILL) while serving `getImportedKeys()` for schemas
  with many tables, which used to abort the whole database-tree load. The plugin skips
  that call for DM connections only; everything else (tables, columns, PKs, indexes,
  routines) is introspected normally. Reference navigation / ER diagrams that rely on
  FKs are therefore unavailable for DM.
- DDL retrieval assumes the Oracle-compatible data dictionary (`SYS.ALL_VIEWS`,
  `SYS.ALL_SOURCE`) which DM8 provides. If your instance is in MySQL compatibility
  mode and lacks these views, view DDL silently falls back to "not available".
- The SQL dialect defaults to `GenericSQL` (the same choice JetBrains ships for Tibero,
  another Oracle-compatible DB). Change it per data source if needed.

## Development

```
./gradlew runIde        # sandbox IDE with the plugin
./gradlew buildPlugin   # distributable zip in build/distributions
./gradlew test          # unit tests
```

Plugin structure — the interesting parts:

```
src/main/resources/config/extra-drivers.xml    # driver definition (com.intellij.database.driversConfig)
src/main/resources/config/extra-artifacts.xml  # driver jar artifact (com.intellij.database.artifactsConfig)
src/main/resources/icons/dm.svg                # driver icon
src/main/java/me/mteach/dameng/
    DmMetadataWrapper.java    # skips FK introspection for DM (jdbcMetadataWrapper)
    DmJdbcSourceLoader.java   # view / routine DDL from data dictionary (jdbcSourceLoader)
    DmDefinitionProvider.java # console Ctrl+Click DDL navigation (definitionProvider)
```

## License

MIT (plugin code). The Dameng JDBC driver is downloaded from
[Maven Central](https://repo1.maven.org/maven2/com/dameng/DmJdbcDriver18/) and remains
under its vendor's terms.
