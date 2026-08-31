package com.example.testing_demo.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for V1__create_tables.sql migration
 * Ensures 100% coverage of schema creation logic
 */
@SpringBootTest
@Transactional
public class V1CreateTablesTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() throws SQLException {
        // Clean up tables if they exist from previous test runs
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS user_account CASCADE");
            jdbcTemplate.execute("DROP TABLE IF EXISTS facility CASCADE");
        } catch (Exception e) {
            // Ignore if tables don't exist
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up after tests
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS user_account CASCADE");
            jdbcTemplate.execute("DROP TABLE IF EXISTS facility CASCADE");
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testFacilityTableExists() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "facility", null);
            assertTrue(tables.next(), "Facility table should exist");
        }
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testFacilityTableHasRequiredColumns() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "facility", null);
            
            List<String> columnNames = new ArrayList<>();
            while (columns.next()) {
                columnNames.add(columns.getString("COLUMN_NAME"));
            }
            
            assertTrue(columnNames.contains("id"), "Facility table should have id column");
            assertTrue(columnNames.contains("name"), "Facility table should have name column");
            assertTrue(columnNames.contains("address"), "Facility table should have address column");
            assertTrue(columnNames.contains("timezone"), "Facility table should have timezone column");
            assertTrue(columnNames.contains("region_code"), "Facility table should have region_code column");
            assertTrue(columnNames.contains("active"), "Facility table should have active column");
            assertTrue(columnNames.contains("created_at"), "Facility table should have created_at column");
            assertTrue(columnNames.contains("updated_at"), "Facility table should have updated_at column");
            assertTrue(columnNames.contains("created_by"), "Facility table should have created_by column");
            assertTrue(columnNames.contains("updated_by"), "Facility table should have updated_by column");
        }
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testFacilityTableHasPrimaryKey() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, "facility");
            
            assertTrue(primaryKeys.next(), "Facility table should have a primary key");
            assertEquals("id", primaryKeys.getString("COLUMN_NAME"), "Primary key should be on id column");
        }
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testFacilityActiveDefaultValue() {
        jdbcTemplate.update("INSERT INTO facility (name, timezone) VALUES (?, ?)", "Test Facility", "America/New_York");
        
        Boolean active = jdbcTemplate.queryForObject(
            "SELECT active FROM facility WHERE name = ?", 
            Boolean.class, 
            "Test Facility"
        );
        
        assertTrue(active, "Active column should default to true");
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testFacilityTimestampDefaults() {
        jdbcTemplate.update("INSERT INTO facility (name, timezone) VALUES (?, ?)", "Test Facility", "America/New_York");
        
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM facility WHERE name = ? AND created_at IS NOT NULL AND updated_at IS NOT NULL", 
            Long.class, 
            "Test Facility"
        );
        
        assertEquals(1L, count, "Timestamp columns should have default values");
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountTableExists() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "user_account", null);
            assertTrue(tables.next(), "User_account table should exist");
        }
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountTableHasRequiredColumns() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "user_account", null);
            
            List<String> columnNames = new ArrayList<>();
            while (columns.next()) {
                columnNames.add(columns.getString("COLUMN_NAME"));
            }
            
            assertTrue(columnNames.contains("id"), "User_account table should have id column");
            assertTrue(columnNames.contains("username"), "User_account table should have username column");
            assertTrue(columnNames.contains("email"), "User_account table should have email column");
            assertTrue(columnNames.contains("password_hash"), "User_account table should have password_hash column");
            assertTrue(columnNames.contains("roles"), "User_account table should have roles column");
            assertTrue(columnNames.contains("facility_id"), "User_account table should have facility_id column");
            assertTrue(columnNames.contains("created_at"), "User_account table should have created_at column");
            assertTrue(columnNames.contains("updated_at"), "User_account table should have updated_at column");
            assertTrue(columnNames.contains("created_by"), "User_account table should have created_by column");
            assertTrue(columnNames.contains("updated_by"), "User_account table should have updated_by column");
        }
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountTableHasPrimaryKey() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, "user_account");
            
            assertTrue(primaryKeys.next(), "User_account table should have a primary key");
            assertEquals("id", primaryKeys.getString("COLUMN_NAME"), "Primary key should be on id column");
        }
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountFacilityForeignKey() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet foreignKeys = metaData.getImportedKeys(null, null, "user_account");
            
            boolean foundFacilityFK = false;
            while (foreignKeys.next()) {
                if ("facility".equals(foreignKeys.getString("PKTABLE_NAME")) &&
                    "facility_id".equals(foreignKeys.getString("FKCOLUMN_NAME"))) {
                    foundFacilityFK = true;
                    break;
                }
            }
            
            assertTrue(foundFacilityFK, "User_account should have foreign key to facility table");
        }
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountFacilityForeignKeyConstraint() {
        jdbcTemplate.update("INSERT INTO facility (name, timezone) VALUES (?, ?)", "Test Facility", "America/New_York");
        Long facilityId = jdbcTemplate.queryForObject("SELECT id FROM facility WHERE name = ?", Long.class, "Test Facility");
        
        jdbcTemplate.update(
            "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?)",
            "testuser", "hash123", "MANAGER", facilityId
        );
        
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_account WHERE username = ?", 
            Long.class, 
            "testuser"
        );
        
        assertEquals(1L, count, "User account should be inserted with valid facility_id");
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountFacilityForeignKeyViolation() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?)",
                "testuser", "hash123", "MANAGER", 99999L
            );
        }, "Should throw exception when inserting user_account with non-existent facility_id");
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountUniqueConstraintFacilityUsername() {
        jdbcTemplate.update("INSERT INTO facility (name, timezone) VALUES (?, ?)", "Test Facility", "America/New_York");
        Long facilityId = jdbcTemplate.queryForObject("SELECT id FROM facility WHERE name = ?", Long.class, "Test Facility");
        
        jdbcTemplate.update(
            "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?)",
            "testuser", "hash123", "MANAGER", facilityId
        );
        
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?)",
                "testuser", "hash456", "STAFF", facilityId
            );
        }, "Should throw exception when inserting duplicate username for same facility");
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountUniqueConstraintAllowsSameUsernameAcrossFacilities() {
        jdbcTemplate.update("INSERT INTO facility (name, timezone) VALUES (?, ?)", "Facility 1", "America/New_York");
        jdbcTemplate.update("INSERT INTO facility (name, timezone) VALUES (?, ?)", "Facility 2", "America/Chicago");
        
        Long facility1Id = jdbcTemplate.queryForObject("SELECT id FROM facility WHERE name = ?", Long.class, "Facility 1");
        Long facility2Id = jdbcTemplate.queryForObject("SELECT id FROM facility WHERE name = ?", Long.class, "Facility 2");
        
        jdbcTemplate.update(
            "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?)",
            "testuser", "hash123", "MANAGER", facility1Id
        );
        
        jdbcTemplate.update(
            "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?)",
            "testuser", "hash456", "STAFF", facility2Id
        );
        
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_account WHERE username = ?", 
            Long.class, 
            "testuser"
        );
        
        assertEquals(2L, count, "Same username should be allowed across different facilities");
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountIndexExists() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet indexes = metaData.getIndexInfo(null, null, "user_account", false, false);
            
            boolean foundIndex = false;
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                if ("idx_user_account_facility_role".equals(indexName)) {
                    foundIndex = true;
                    break;
                }
            }
            
            assertTrue(foundIndex, "Index idx_user_account_facility_role should exist");
        }
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountTimestampDefaults() {
        jdbcTemplate.update("INSERT INTO facility (name, timezone) VALUES (?, ?)", "Test Facility", "America/New_York");
        Long facilityId = jdbcTemplate.queryForObject("SELECT id FROM facility WHERE name = ?", Long.class, "Test Facility");
        
        jdbcTemplate.update(
            "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?)",
            "testuser", "hash123", "MANAGER", facilityId
        );
        
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_account WHERE username = ? AND created_at IS NOT NULL AND updated_at IS NOT NULL", 
            Long.class, 
            "testuser"
        );
        
        assertEquals(1L, count, "Timestamp columns should have default values");
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testFacilityNameNotNull() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update("INSERT INTO facility (name, timezone) VALUES (?, ?)", null, "America/New_York");
        }, "Should throw exception when inserting facility with null name");
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testFacilityTimezoneNotNull() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update("INSERT INTO facility (name, timezone) VALUES (?, ?)", "Test Facility", null);
        }, "Should throw exception when inserting facility with null timezone");
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountUsernameNotNull() {
        jdbcTemplate.update("INSERT INTO facility (name, timezone) VALUES (?, ?)", "Test Facility", "America/New_York");
        Long facilityId = jdbcTemplate.queryForObject("SELECT id FROM facility WHERE name = ?", Long.class, "Test Facility");
        
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?)",
                null, "hash123", "MANAGER", facilityId
            );
        }, "Should throw exception when inserting user_account with null username");
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountPasswordHashNotNull() {
        jdbcTemplate.update("INSERT INTO facility (name, timezone) VALUES (?, ?)", "Test Facility", "America/New_York");
        Long facilityId = jdbcTemplate.queryForObject("SELECT id FROM facility WHERE name = ?", Long.class, "Test Facility");
        
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?)",
                "testuser", null, "MANAGER", facilityId
            );
        }, "Should throw exception when inserting user_account with null password_hash");
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountRolesNotNull() {
        jdbcTemplate.update("INSERT INTO facility (name, timezone) VALUES (?, ?)", "Test Facility", "America/New_York");
        Long facilityId = jdbcTemplate.queryForObject("SELECT id FROM facility WHERE name = ?", Long.class, "Test Facility");
        
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?)",
                "testuser", "hash123", null, facilityId
            );
        }, "Should throw exception when inserting user_account with null roles");
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountFacilityIdNotNull() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?)",
                "testuser", "hash123", "MANAGER", null
            );
        }, "Should throw exception when inserting user_account with null facility_id");
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testFacilityTableCommentExists() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT obj_description('facility'::regclass, 'pg_class') AS comment"
            );
            assertTrue(rs.next(), "Facility table should have a comment");
            assertNotNull(rs.getString("comment"), "Facility table comment should not be null");
        }
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testUserAccountTableCommentExists() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT obj_description('user_account'::regclass, 'pg_class') AS comment"
            );
            assertTrue(rs.next(), "User_account table should have a comment");
            assertNotNull(rs.getString("comment"), "User_account table comment should not be null");
        }
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testNoConflictWithV3StaffTableSchema() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "facility", "id");
            
            assertTrue(columns.next(), "Facility.id column should exist for V3 staff table foreign key reference");
        }
    }

    @Test
    @Sql(scripts = "/db/migration/V1__create_tables.sql")
    public void testFacilityIdColumnTypeCompatibleWithForeignKeys() throws SQLException {
        jdbcTemplate.update("INSERT INTO facility (name, timezone) VALUES (?, ?)", "Test Facility", "America/New_York");
        Long facilityId = jdbcTemplate.queryForObject("SELECT id FROM facility WHERE name = ?", Long.class, "Test Facility");
        
        assertNotNull(facilityId, "Facility id should be generated as BIGSERIAL");
        assertTrue(facilityId > 0, "Facility id should be positive");
    }
}