package com.example.testingdemo.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for V1__create_tables.sql migration
 * Ensures 100% coverage of database schema creation for test environment
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class V1CreateTablesTest {

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        
        // Clean up any existing tables before migration
        jdbcTemplate.execute("DROP TABLE IF EXISTS user_account CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS facility CASCADE");
        
        // Run the migration
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();
        flyway.clean();
        flyway.migrate();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS user_account CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS facility CASCADE");
    }

    @Test
    void testFacilityTableExists() {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'facility')";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class);
        assertTrue(exists, "Facility table should exist after migration");
    }

    @Test
    void testUserAccountTableExists() {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'user_account')";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class);
        assertTrue(exists, "User_account table should exist after migration");
    }

    @Test
    void testFacilityTableStructure() {
        String sql = "SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_name = 'facility' ORDER BY ordinal_position";
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertFalse(columns.isEmpty(), "Facility table should have columns");
        assertTrue(columns.stream().anyMatch(col -> "id".equals(col.get("column_name"))), "Facility table should have id column");
        assertTrue(columns.stream().anyMatch(col -> "name".equals(col.get("column_name"))), "Facility table should have name column");
        assertTrue(columns.stream().anyMatch(col -> "address".equals(col.get("column_name"))), "Facility table should have address column");
        assertTrue(columns.stream().anyMatch(col -> "timezone".equals(col.get("column_name"))), "Facility table should have timezone column");
        assertTrue(columns.stream().anyMatch(col -> "region_code".equals(col.get("column_name"))), "Facility table should have region_code column");
        assertTrue(columns.stream().anyMatch(col -> "active".equals(col.get("column_name"))), "Facility table should have active column");
        assertTrue(columns.stream().anyMatch(col -> "created_at".equals(col.get("column_name"))), "Facility table should have created_at column");
        assertTrue(columns.stream().anyMatch(col -> "updated_at".equals(col.get("column_name"))), "Facility table should have updated_at column");
        assertTrue(columns.stream().anyMatch(col -> "created_by".equals(col.get("column_name"))), "Facility table should have created_by column");
        assertTrue(columns.stream().anyMatch(col -> "updated_by".equals(col.get("column_name"))), "Facility table should have updated_by column");
    }

    @Test
    void testUserAccountTableStructure() {
        String sql = "SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_name = 'user_account' ORDER BY ordinal_position";
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertFalse(columns.isEmpty(), "User_account table should have columns");
        assertTrue(columns.stream().anyMatch(col -> "id".equals(col.get("column_name"))), "User_account table should have id column");
        assertTrue(columns.stream().anyMatch(col -> "username".equals(col.get("column_name"))), "User_account table should have username column");
        assertTrue(columns.stream().anyMatch(col -> "email".equals(col.get("column_name"))), "User_account table should have email column");
        assertTrue(columns.stream().anyMatch(col -> "password_hash".equals(col.get("column_name"))), "User_account table should have password_hash column");
        assertTrue(columns.stream().anyMatch(col -> "roles".equals(col.get("column_name"))), "User_account table should have roles column");
        assertTrue(columns.stream().anyMatch(col -> "facility_id".equals(col.get("column_name"))), "User_account table should have facility_id column");
        assertTrue(columns.stream().anyMatch(col -> "created_at".equals(col.get("column_name"))), "User_account table should have created_at column");
        assertTrue(columns.stream().anyMatch(col -> "updated_at".equals(col.get("column_name"))), "User_account table should have updated_at column");
        assertTrue(columns.stream().anyMatch(col -> "created_by".equals(col.get("column_name"))), "User_account table should have created_by column");
        assertTrue(columns.stream().anyMatch(col -> "updated_by".equals(col.get("column_name"))), "User_account table should have updated_by column");
    }

    @Test
    void testFacilityPrimaryKeyConstraint() {
        String sql = "SELECT constraint_name FROM information_schema.table_constraints WHERE table_name = 'facility' AND constraint_type = 'PRIMARY KEY'";
        List<String> constraints = jdbcTemplate.queryForList(sql, String.class);
        
        assertFalse(constraints.isEmpty(), "Facility table should have a primary key constraint");
    }

    @Test
    void testUserAccountPrimaryKeyConstraint() {
        String sql = "SELECT constraint_name FROM information_schema.table_constraints WHERE table_name = 'user_account' AND constraint_type = 'PRIMARY KEY'";
        List<String> constraints = jdbcTemplate.queryForList(sql, String.class);
        
        assertFalse(constraints.isEmpty(), "User_account table should have a primary key constraint");
    }

    @Test
    void testUserAccountForeignKeyConstraint() {
        String sql = "SELECT constraint_name FROM information_schema.table_constraints WHERE table_name = 'user_account' AND constraint_type = 'FOREIGN KEY'";
        List<String> constraints = jdbcTemplate.queryForList(sql, String.class);
        
        assertFalse(constraints.isEmpty(), "User_account table should have a foreign key constraint");
        assertTrue(constraints.stream().anyMatch(c -> c.contains("fk_user_facility")), "Foreign key constraint should be named fk_user_facility");
    }

    @Test
    void testUserAccountUniqueConstraint() {
        String sql = "SELECT constraint_name FROM information_schema.table_constraints WHERE table_name = 'user_account' AND constraint_type = 'UNIQUE'";
        List<String> constraints = jdbcTemplate.queryForList(sql, String.class);
        
        assertFalse(constraints.isEmpty(), "User_account table should have a unique constraint");
        assertTrue(constraints.stream().anyMatch(c -> c.contains("uk_user_facility_username")), "Unique constraint should be named uk_user_facility_username");
    }

    @Test
    void testUserAccountIndexExists() {
        String sql = "SELECT indexname FROM pg_indexes WHERE tablename = 'user_account' AND indexname = 'idx_user_account_facility_role'";
        List<String> indexes = jdbcTemplate.queryForList(sql, String.class);
        
        assertFalse(indexes.isEmpty(), "Index idx_user_account_facility_role should exist on user_account table");
    }

    @Test
    void testInsertFacilityWithDefaultValues() {
        String insertSql = "INSERT INTO facility (name, timezone) VALUES (?, ?) RETURNING id";
        Long facilityId = jdbcTemplate.queryForObject(insertSql, Long.class, "Test Facility", "UTC");
        
        assertNotNull(facilityId, "Facility ID should be generated");
        
        String selectSql = "SELECT active, created_at, updated_at FROM facility WHERE id = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(selectSql, facilityId);
        
        assertTrue((Boolean) result.get("active"), "Default active value should be true");
        assertNotNull(result.get("created_at"), "Default created_at should be set");
        assertNotNull(result.get("updated_at"), "Default updated_at should be set");
    }

    @Test
    void testInsertUserAccountWithValidFacility() {
        // First insert a facility
        String facilityInsertSql = "INSERT INTO facility (name, timezone) VALUES (?, ?) RETURNING id";
        Long facilityId = jdbcTemplate.queryForObject(facilityInsertSql, Long.class, "Test Facility", "UTC");
        
        // Then insert a user
        String userInsertSql = "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?) RETURNING id";
        Long userId = jdbcTemplate.queryForObject(userInsertSql, Long.class, "testuser", "hashedpassword", "USER", facilityId);
        
        assertNotNull(userId, "User ID should be generated");
        
        String selectSql = "SELECT username, facility_id FROM user_account WHERE id = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(selectSql, userId);
        
        assertEquals("testuser", result.get("username"), "Username should match");
        assertEquals(facilityId, ((Number) result.get("facility_id")).longValue(), "Facility ID should match");
    }

    @Test
    void testForeignKeyConstraintOnDelete() {
        // Insert facility
        String facilityInsertSql = "INSERT INTO facility (name, timezone) VALUES (?, ?) RETURNING id";
        Long facilityId = jdbcTemplate.queryForObject(facilityInsertSql, Long.class, "Test Facility", "UTC");
        
        // Insert user
        String userInsertSql = "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?) RETURNING id";
        Long userId = jdbcTemplate.queryForObject(userInsertSql, Long.class, "testuser", "hashedpassword", "USER", facilityId);
        
        // Delete facility (should cascade to user)
        String deleteFacilitySql = "DELETE FROM facility WHERE id = ?";
        jdbcTemplate.update(deleteFacilitySql, facilityId);
        
        // Verify user is also deleted
        String countSql = "SELECT COUNT(*) FROM user_account WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, userId);
        
        assertEquals(0, count, "User should be deleted when facility is deleted (CASCADE)");
    }

    @Test
    void testUniqueConstraintViolation() {
        // Insert facility
        String facilityInsertSql = "INSERT INTO facility (name, timezone) VALUES (?, ?) RETURNING id";
        Long facilityId = jdbcTemplate.queryForObject(facilityInsertSql, Long.class, "Test Facility", "UTC");
        
        // Insert first user
        String userInsertSql = "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(userInsertSql, "testuser", "hashedpassword", "USER", facilityId);
        
        // Try to insert duplicate user with same username and facility_id
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(userInsertSql, "testuser", "hashedpassword2", "ADMIN", facilityId);
        }, "Should throw exception when violating unique constraint on facility_id and username");
    }

    @Test
    void testFacilityTableComment() {
        String sql = "SELECT obj_description('facility'::regclass)";
        String comment = jdbcTemplate.queryForObject(sql, String.class);
        
        assertNotNull(comment, "Facility table should have a comment");
        assertEquals("Facility master table for test environment", comment, "Facility table comment should match");
    }

    @Test
    void testUserAccountTableComment() {
        String sql = "SELECT obj_description('user_account'::regclass)";
        String comment = jdbcTemplate.queryForObject(sql, String.class);
        
        assertNotNull(comment, "User_account table should have a comment");
        assertEquals("User account table for test environment with test user data", comment, "User_account table comment should match");
    }

    @Test
    void testMigrationDoesNotConflictWithV3() {
        // Verify that staff table does NOT exist in test resources
        String sql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'staff')";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class);
        
        assertFalse(exists, "Staff table should NOT exist in test migration (no conflict with V3__create_staff_table.sql)");
    }

    @Test
    void testTestDatabaseSchemaIsIsolated() {
        // Verify only expected tables exist
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE' ORDER BY table_name";
        List<String> tables = jdbcTemplate.queryForList(sql, String.class);
        
        assertTrue(tables.contains("facility"), "Facility table should exist");
        assertTrue(tables.contains("user_account"), "User_account table should exist");
        assertTrue(tables.contains("flyway_schema_history"), "Flyway history table should exist");
        
        // Ensure no unexpected tables from main resources
        assertFalse(tables.stream().anyMatch(t -> t.equals("staff")), "Staff table should not exist in test schema");
    }

    @Test
    void testNotNullConstraintsOnFacility() {
        String facilityInsertSql = "INSERT INTO facility (name, timezone) VALUES (?, ?)";
        
        // Test name NOT NULL
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(facilityInsertSql, null, "UTC");
        }, "Should throw exception when name is null");
        
        // Test timezone NOT NULL
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(facilityInsertSql, "Test Facility", null);
        }, "Should throw exception when timezone is null");
    }

    @Test
    void testNotNullConstraintsOnUserAccount() {
        // Insert facility first
        String facilityInsertSql = "INSERT INTO facility (name, timezone) VALUES (?, ?) RETURNING id";
        Long facilityId = jdbcTemplate.queryForObject(facilityInsertSql, Long.class, "Test Facility", "UTC");
        
        String userInsertSql = "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES (?, ?, ?, ?)";
        
        // Test username NOT NULL
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(userInsertSql, null, "hashedpassword", "USER", facilityId);
        }, "Should throw exception when username is null");
        
        // Test password_hash NOT NULL
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(userInsertSql, "testuser", null, "USER", facilityId);
        }, "Should throw exception when password_hash is null");
        
        // Test roles NOT NULL
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(userInsertSql, "testuser", "hashedpassword", null, facilityId);
        }, "Should throw exception when roles is null");
        
        // Test facility_id NOT NULL
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(userInsertSql, "testuser", "hashedpassword", "USER", null);
        }, "Should throw exception when facility_id is null");
    }
}