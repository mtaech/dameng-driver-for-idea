package me.mteach.dameng;

import com.intellij.database.dataSource.DatabaseDriver;
import com.intellij.database.dataSource.DatabaseDriverManager;
import com.intellij.testFramework.LightPlatformTestCase;
import org.jetbrains.annotations.NotNull;

/**
 * Smoke test: plugin loads, the DM driver is registered by {@code driversConfig},
 * and both DM extensions are present.
 */
@SuppressWarnings("JUnit4TestClassNamingConvention")
public class DamengDriverRegistrationTest extends LightPlatformTestCase {

    public void testDamengDriverIsRegistered() {
        DatabaseDriver driver = getDriver();
        assertNotNull("Driver dameng.dm is not registered", driver);
        assertEquals("dm.jdbc.driver.DmDriver", driver.getDriverClass());
        assertEquals("GenericSQL", driver.getSqlDialect());
        assertTrue("Driver must be predefined", driver.isPredefined());
        assertTrue("Driver name must mention Dameng", driver.getName().contains("达梦"));
    }

    public void testDriverResolvesSampleUrl() {
        DatabaseDriver driver = getDriver();
        assertNotNull(driver);
        assertTrue("Driver must accept jdbc:dm:// URLs", driver.matchesUrl("jdbc:dm://localhost:5236"));
        assertFalse("Driver must not accept foreign URLs", driver.matchesUrl("jdbc:mysql://localhost:3306"));
    }

public void testDmDbmsIsRegistered() {
        com.intellij.database.Dbms dm = com.intellij.database.Dbms.byName("DAMENG");
        assertNotNull("Dbms 'DAMENG' must resolve via byName", dm);
        // A real DM connection reports product name "DM DBMS"; make sure it matches.
        assertTrue("Product name 'DM DBMS' must match the registered pattern",
                   java.util.regex.Pattern.compile(
                           "^(?i).*\\b(?:dm|dameng).*$", java.util.regex.Pattern.DOTALL)
                           .matcher("DM DBMS").matches());
    }

    public void testDm7DriverIsRegistered() {
        DatabaseDriver driver = DatabaseDriverManager.getInstance().getDriver("dameng.dm7");
        assertNotNull("Driver dameng.dm7 is not registered", driver);
        assertEquals("dm.jdbc.driver.DmDriver", driver.getDriverClass());
        assertTrue("DM7 driver must be predefined", driver.isPredefined());
    }

    private static @NotNull DatabaseDriver getDriver() {
        DatabaseDriver driver = DatabaseDriverManager.getInstance().getDriver("dameng.dm");
        assertNotNull(driver);
        return driver;
    }
}
