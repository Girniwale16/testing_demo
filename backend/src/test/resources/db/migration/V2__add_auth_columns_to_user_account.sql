package com.example.testingdemo.db.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class V2AddAuthColumnsToUserAccountTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clean and migrate database
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
        flyway.clean();
        flyway.migrate();
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        jdbcTemplate.execute("DELETE FROM user_account WHERE email LIKE 'test%@example.com'");
    }

    @Test
    void testLastLoginAtColumnExists() {
        String sql = "SELECT column_name, data_type, is_nullable " +
                     "FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name = 'last_login_at'";
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertFalse(columns.isEmpty(), "last_login_at column should exist");
        assertEquals("last_login_at", columns.get(0).get("column_name"));
        assertTrue(columns.get(0).get("data_type").toString().contains("timestamp"));
        assertEquals("YES", columns.get(0).get("is_nullable"));
    }

    @Test
    void testIsActiveColumnExistsWithDefaultTrue() {
        String sql = "SELECT column_name, data_type, is_nullable, column_default " +
                     "FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name = 'is_active'";
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertFalse(columns.isEmpty(), "is_active column should exist");
        assertEquals("is_active", columns.get(0).get("column_name"));
        assertEquals("boolean", columns.get(0).get("data_type"));
        assertEquals("NO", columns.get(0).get("is_nullable"));
        assertTrue(columns.get(0).get("column_default").toString().contains("true"));
    }

    @Test
    void testEmploymentStatusColumnExists() {
        String sql = "SELECT column_name, data_type, character_maximum_length " +
                     "FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name = 'employment_status'";
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertFalse(columns.isEmpty(), "employment_status column should exist");
        assertEquals("employment_status", columns.get(0).get("column_name"));
        assertEquals("character varying", columns.get(0).get("data_type"));
        assertEquals(20, columns.get(0).get("character_maximum_length"));
    }

    @Test
    void testEndDateColumnExists() {
        String sql = "SELECT column_name, data_type, is_nullable " +
                     "FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name = 'end_date'";
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertFalse(columns.isEmpty(), "end_date column should exist");
        assertEquals("end_date", columns.get(0).get("column_name"));
        assertEquals("date", columns.get(0).get("data_type"));
        assertEquals("YES", columns.get(0).get("is_nullable"));
    }

    @Test
    void testEmploymentStatusCheckConstraintExists() {
        String sql = "SELECT constraint_name, check_clause " +
                     "FROM information_schema.check_constraints " +
                     "WHERE constraint_name = 'chk_user_employment_status'";
        
        List<Map<String, Object>> constraints = jdbcTemplate.queryForList(sql);
        
        assertFalse(constraints.isEmpty(), "Check constraint chk_user_employment_status should exist");
        String checkClause = constraints.get(0).get("check_clause").toString();
        assertTrue(checkClause.contains("ACTIVE") && checkClause.contains("INACTIVE"));
    }

    @Test
    void testEmploymentStatusConstraintAllowsActiveValue() {
        jdbcTemplate.execute("INSERT INTO user_account (email, password_hash, employment_status) " +
                           "VALUES ('test_active@example.com', 'hash123', 'ACTIVE')");
        
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_account WHERE email = 'test_active@example.com' AND employment_status = 'ACTIVE'",
            Integer.class
        );
        
        assertEquals(1, count);
    }

    @Test
    void testEmploymentStatusConstraintAllowsInactiveValue() {
        jdbcTemplate.execute("INSERT INTO user_account (email, password_hash, employment_status) " +
                           "VALUES ('test_inactive@example.com', 'hash123', 'INACTIVE')");
        
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_account WHERE email = 'test_inactive@example.com' AND employment_status = 'INACTIVE'",
            Integer.class
        );
        
        assertEquals(1, count);
    }

    @Test
    void testEmploymentStatusConstraintRejectsInvalidValue() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("INSERT INTO user_account (email, password_hash, employment_status) " +
                               "VALUES ('test_invalid@example.com', 'hash123', 'TERMINATED')");
        }, "Should reject invalid employment_status value");
    }

    @Test
    void testIsActiveIndexExists() {
        String sql = "SELECT indexname FROM pg_indexes " +
                     "WHERE tablename = 'user_account' AND indexname = 'idx_user_account_is_active'";
        
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(sql);
        
        assertFalse(indexes.isEmpty(), "Index idx_user_account_is_active should exist");
    }

    @Test
    void testEmploymentStatusIndexExists() {
        String sql = "SELECT indexname FROM pg_indexes " +
                     "WHERE tablename = 'user_account' AND indexname = 'idx_user_account_employment_status'";
        
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(sql);
        
        assertFalse(indexes.isEmpty(), "Index idx_user_account_employment_status should exist");
    }

    @Test
    void testIsActiveDefaultValueAppliedOnInsert() {
        jdbcTemplate.execute("INSERT INTO user_account (email, password_hash) " +
                           "VALUES ('test_default@example.com', 'hash123')");
        
        Boolean isActive = jdbcTemplate.queryForObject(
            "SELECT is_active FROM user_account WHERE email = 'test_default@example.com'",
            Boolean.class
        );
        
        assertTrue(isActive, "is_active should default to true");
    }

    @Test
    void testCanFilterByIsActiveColumn() {
        jdbcTemplate.execute("INSERT INTO user_account (email, password_hash, is_active) " +
                           "VALUES ('test_active1@example.com', 'hash123', true)");
        jdbcTemplate.execute("INSERT INTO user_account (email, password_hash, is_active) " +
                           "VALUES ('test_inactive1@example.com', 'hash123', false)");
        
        Integer activeCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_account WHERE is_active = true AND email LIKE 'test%@example.com'",
            Integer.class
        );
        Integer inactiveCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_account WHERE is_active = false AND email LIKE 'test%@example.com'",
            Integer.class
        );
        
        assertTrue(activeCount >= 1, "Should find at least one active user");
        assertTrue(inactiveCount >= 1, "Should find at least one inactive user");
    }

    @Test
    void testLastLoginAtCanBeUpdated() {
        jdbcTemplate.execute("INSERT INTO user_account (email, password_hash) " +
                           "VALUES ('test_login@example.com', 'hash123')");
        
        jdbcTemplate.update("UPDATE user_account SET last_login_at = ? WHERE email = ?",
                          LocalDateTime.now(), "test_login@example.com");
        
        LocalDateTime lastLogin = jdbcTemplate.queryForObject(
            "SELECT last_login_at FROM user_account WHERE email = 'test_login@example.com'",
            LocalDateTime.class
        );
        
        assertNotNull(lastLogin, "last_login_at should be set");
    }

    @Test
    void testEndDateCanBeSet() {
        jdbcTemplate.execute("INSERT INTO user_account (email, password_hash, end_date) " +
                           "VALUES ('test_enddate@example.com', 'hash123', '2024-12-31')");
        
        String endDate = jdbcTemplate.queryForObject(
            "SELECT end_date::text FROM user_account WHERE email = 'test_enddate@example.com'",
            String.class
        );
        
        assertNotNull(endDate, "end_date should be set");
        assertTrue(endDate.contains("2024-12-31"));
    }

    @Test
    void testColumnCommentsExist() {
        String sql = "SELECT column_name, col_description((table_schema||'.'||table_name)::regclass::oid, ordinal_position) as comment " +
                     "FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name IN ('last_login_at', 'is_active', 'employment_status', 'end_date')";
        
        List<Map<String, Object>> comments = jdbcTemplate.queryForList(sql);
        
        assertEquals(4, comments.size(), "All four columns should have comments");
        
        for (Map<String, Object> row : comments) {
            assertNotNull(row.get("comment"), "Column " + row.get("column_name") + " should have a comment");
        }
    }

    @Test
    void testCanDeactivateUserAccountIndependently() {
        jdbcTemplate.execute("INSERT INTO user_account (email, password_hash, is_active, employment_status) " +
                           "VALUES ('test_deactivate@example.com', 'hash123', false, 'INACTIVE')");
        
        Map<String, Object> user = jdbcTemplate.queryForMap(
            "SELECT is_active, employment_status FROM user_account WHERE email = 'test_deactivate@example.com'"
        );
        
        assertFalse((Boolean) user.get("is_active"));
        assertEquals("INACTIVE", user.get("employment_status"));
    }

    @Test
    void testIsActiveNotNullConstraint() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("INSERT INTO user_account (email, password_hash, is_active) " +
                               "VALUES ('test_null@example.com', 'hash123', NULL)");
        }, "is_active should not accept NULL values");
    }

    @Test
    void testAllColumnsCanBeUsedTogether() {
        jdbcTemplate.execute("INSERT INTO user_account (email, password_hash, last_login_at, is_active, employment_status, end_date) " +
                           "VALUES ('test_complete@example.com', 'hash123', '2024-01-15 10:30:00', true, 'ACTIVE', NULL)");
        
        Map<String, Object> user = jdbcTemplate.queryForMap(
            "SELECT email, last_login_at, is_active, employment_status, end_date FROM user_account WHERE email = 'test_complete@example.com'"
        );
        
        assertEquals("test_complete@example.com", user.get("email"));
        assertNotNull(user.get("last_login_at"));
        assertTrue((Boolean) user.get("is_active"));
        assertEquals("ACTIVE", user.get("employment_status"));
        assertNull(user.get("end_date"));
    }
}