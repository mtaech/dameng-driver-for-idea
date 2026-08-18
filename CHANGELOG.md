# Changelog

## [Unreleased]

### Added

- Initial Dameng (达梦, DM) database support for DataGrip and IntelliJ IDEA Ultimate:
  - Dedicated `Dbms` instance (`id=DM`) registered via `<dbms>` extension: DM
    connections are detected by product name ("DM DBMS") instead of falling back
    to UNKNOWN; all DM-specific database extensions are scoped to `dbms="DM"`.
  - `DM (达梦数据库)` JDBC driver (`dm.jdbc.driver.DmDriver`) registered with a dedicated icon,
    `jdbc:dm://host:5236` URL template (with `schema` / `compatibleMode` query parameters),
    default port 5236 and keep-alive query.
  - `DM7 (达梦数据库 7)` driver entry using `Dm7JdbcDriver17` 7.6.0.165.
  - `DmJdbcDriver18` 8.1.3.140 artifact resolved from Maven Central automatically when
    a data source is created — no manual jar download.
  - View DDL support: definitions loaded from DM8's data dictionary
    (`SYS.ALL_VIEWS`), procedures/functions from `SYS.ALL_SOURCE`.
  - Console navigation: Ctrl+Click / Go to DDL on a table name opens its DDL
    via DM8's Oracle-compatible `DBMS_METADATA.GET_DDL` package.
- Foreign-key introspection is skipped for DM data sources: the Dameng driver can
  exhaust the remote JDBC bridge process while serving `getImportedKeys()`, which
  aborted the whole database-tree introspection (tables appeared missing).

### Notes

- The SQL dialect for DM data sources defaults to `GenericSQL` (same choice JetBrains
  made for Tibero). Switch the dialect per data source if you prefer Oracle-style
  highlighting for an Oracle-compatibility-mode instance.
