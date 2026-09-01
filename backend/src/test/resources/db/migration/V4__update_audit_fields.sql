package com.example.testingdemo.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.flyway.enabled=false"
})
class V4UpdateAuditFieldsMigrationTest {

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;
    private Flyway flyway;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        
        flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .cleanDisabled(false)
            .load();
        
        flyway.clean();
        flyway.migrate();
    }

    @AfterEach
    void tearDown() {
        if (flyway != null) {
            flyway.clean();
        }
    }

    @Test
    void testFacilityCreatedByColumnTypeIsVarchar100() {
        String sql = "SELECT data_type, character_maximum_length FROM information_schema.columns " +
                     "WHERE table_name = 'facility' AND column_name = 'created_by'";
        
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        
        assertEquals("character varying", result.get("data_type"));
        assertEquals(100, result.get("character_maximum_length"));
    }

    @Test
    void testFacilityCreatedByHasDefaultValue() {
        String sql = "SELECT column_default FROM information_schema.columns " +
                     "WHERE table_name = 'facility' AND column_name = 'created_by'";
        
        String defaultValue = jdbcTemplate.queryForObject(sql, String.class);
        
        assertTrue(defaultValue.contains("system"));
    }

    @Test
    void testFacilityCreatedByIsNotNull() {
        String sql = "SELECT is_nullable FROM information_schema.columns " +
                     "WHERE table_name = 'facility' AND column_name = 'created_by'";
        
        String isNullable = jdbcTemplate.queryForObject(sql, String.class);
        
        assertEquals("NO", isNullable);
    }

    @Test
    void testFacilityUpdatedByColumnTypeIsVarchar100() {
        String sql = "SELECT data_type, character_maximum_length FROM information_schema.columns " +
                     "WHERE table_name = 'facility' AND column_name = 'updated_by'";
        
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        
        assertEquals("character varying", result.get("data_type"));
        assertEquals(100, result.get("character_maximum_length"));
    }

    @Test
    void testFacilityCreatedAtIsNotNull() {
        String sql = "SELECT is_nullable FROM information_schema.columns " +
                     "WHERE table_name = 'facility' AND column_name = 'created_at'";
        
        String isNullable = jdbcTemplate.queryForObject(sql, String.class);
        
        assertEquals("NO", isNullable);
    }

    @Test
    void testFacilityUpdatedAtIsNotNull() {
        String sql = "SELECT is_nullable FROM information_schema.columns " +
                     "WHERE table_name = 'facility' AND column_name = 'updated_at'";
        
        String isNullable = jdbcTemplate.queryForObject(sql, String.class);
        
        assertEquals("NO", isNullable);
    }

    @Test
    void testUserAccountCreatedByColumnTypeIsVarchar100() {
        String sql = "SELECT data_type, character_maximum_length FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name = 'created_by'";
        
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        
        assertEquals("character varying", result.get("data_type"));
        assertEquals(100, result.get("character_maximum_length"));
    }

    @Test
    void testUserAccountCreatedByHasDefaultValue() {
        String sql = "SELECT column_default FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name = 'created_by'";
        
        String defaultValue = jdbcTemplate.queryForObject(sql, String.class);
        
        assertTrue(defaultValue.contains("system"));
    }

    @Test
    void testUserAccountCreatedByIsNotNull() {
        String sql = "SELECT is_nullable FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name = 'created_by'";
        
        String isNullable = jdbcTemplate.queryForObject(sql, String.class);
        
        assertEquals("NO", isNullable);
    }

    @Test
    void testUserAccountUpdatedByColumnTypeIsVarchar100() {
        String sql = "SELECT data_type, character_maximum_length FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name = 'updated_by'";
        
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        
        assertEquals("character varying", result.get("data_type"));
        assertEquals(100, result.get("character_maximum_length"));
    }

    @Test
    void testUserAccountCreatedAtIsNotNull() {
        String sql = "SELECT is_nullable FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name = 'created_at'";
        
        String isNullable = jdbcTemplate.queryForObject(sql, String.class);
        
        assertEquals("NO", isNullable);
    }

    @Test
    void testUserAccountUpdatedAtIsNotNull() {
        String sql = "SELECT is_nullable FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name = 'updated_at'";
        
        String isNullable = jdbcTemplate.queryForObject(sql, String.class);
        
        assertEquals("NO", isNullable);
    }

    @Test
    void testUserAccountStaffMemberIdColumnExists() {
        String sql = "SELECT COUNT(*) FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name = 'staff_member_id'";
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertEquals(1, count);
    }

    @Test
    void testUserAccountAccountStatusColumnExists() {
        String sql = "SELECT data_type, character_maximum_length FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name = 'account_status'";
        
        Map<String, Object> result = jdbcTemplate.queryForMap(sql);
        
        assertEquals("character varying", result.get("data_type"));
        assertEquals(20, result.get("character_maximum_length"));
    }

    @Test
    void testUserAccountAccountEndDateColumnExists() {
        String sql = "SELECT data_type FROM information_schema.columns " +
                     "WHERE table_name = 'user_account' AND column_name = 'account_end_date'";
        
        String dataType = jdbcTemplate.queryForObject(sql, String.class);
        
        assertEquals("date", dataType);
    }

    @Test
    void testStaffCreatedAtIsNotNull() {
        String sql = "SELECT is_nullable FROM information_schema.columns " +
                     "WHERE table_name = 'staff' AND column_name = 'created_at'";
        
        String isNullable = jdbcTemplate.queryForObject(sql, String.class);
        
        assertEquals("NO", isNullable);
    }

    @Test
    void testStaffUpdatedAtIsNotNull() {
        String sql = "SELECT is_nullable FROM information_schema.columns " +
                     "WHERE table_name = 'staff' AND column_name = 'updated_at'";
        
        String isNullable = jdbcTemplate.queryForObject(sql, String.class);
        
        assertEquals("NO", isNullable);
    }

    @Test
    void testFacilityCreatedAtIndexExists() {
        String sql = "SELECT COUNT(*) FROM pg_indexes " +
                     "WHERE tablename = 'facility' AND indexname = 'idx_facility_created_at'";
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertEquals(1, count);
    }

    @Test
    void testFacilityUpdatedAtIndexExists() {
        String sql = "SELECT COUNT(*) FROM pg_indexes " +
                     "WHERE tablename = 'facility' AND indexname = 'idx_facility_updated_at'";
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertEquals(1, count);
    }

    @Test
    void testUserAccountCreatedAtIndexExists() {
        String sql = "SELECT COUNT(*) FROM pg_indexes " +
                     "WHERE tablename = 'user_account' AND indexname = 'idx_user_account_created_at'";
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertEquals(1, count);
    }

    @Test
    void testUserAccountUpdatedAtIndexExists() {
        String sql = "SELECT COUNT(*) FROM pg_indexes " +
                     "WHERE tablename = 'user_account' AND indexname = 'idx_user_account_updated_at'";
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertEquals(1, count);
    }

    @Test
    void testStaffCreatedAtIndexExists() {
        String sql = "SELECT COUNT(*) FROM pg_indexes " +
                     "WHERE tablename = 'staff' AND indexname = 'idx_staff_created_at'";
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertEquals(1, count);
    }

    @Test
    void testStaffUpdatedAtIndexExists() {
        String sql = "SELECT COUNT(*) FROM pg_indexes " +
                     "WHERE tablename = 'staff' AND indexname = 'idx_staff_updated_at'";
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertEquals(1, count);
    }

    @Test
    void testFacilityInsertWithoutCreatedByUsesDefault() {
        jdbcTemplate.execute("INSERT INTO facility (name, created_at, updated_at) " +
                           "VALUES ('Test Facility', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        
        String sql = "SELECT created_by FROM facility WHERE name = 'Test Facility'";
        String createdBy = jdbcTemplate.queryForObject(sql, String.class);
        
        assertEquals("system", createdBy);
    }

    @Test
    void testUserAccountInsertWithoutCreatedByUsesDefault() {
        jdbcTemplate.execute("INSERT INTO user_account (username, email, created_at, updated_at) " +
                           "VALUES ('testuser', 'test@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        
        String sql = "SELECT created_by FROM user_account WHERE username = 'testuser'";
        String createdBy = jdbcTemplate.queryForObject(sql, String.class);
        
        assertEquals("system", createdBy);
    }

    @Test
    void testFacilityCannotInsertNullCreatedAt() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("INSERT INTO facility (name, created_by, updated_at) " +
                               "VALUES ('Test Facility', 'admin', CURRENT_TIMESTAMP)");
        });
    }

    @Test
    void testFacilityCannotInsertNullUpdatedAt() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("INSERT INTO facility (name, created_by, created_at) " +
                               "VALUES ('Test Facility', 'admin', CURRENT_TIMESTAMP)");
        });
    }

    @Test
    void testUserAccountCannotInsertNullCreatedAt() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("INSERT INTO user_account (username, email, created_by, updated_at) " +
                               "VALUES ('testuser', 'test@example.com', 'admin', CURRENT_TIMESTAMP)");
        });
    }

    @Test
    void testUserAccountCannotInsertNullUpdatedAt() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("INSERT INTO user_account (username, email, created_by, created_at) " +
                               "VALUES ('testuser', 'test@example.com', 'admin', CURRENT_TIMESTAMP)");
        });
    }

    @Test
    void testStaffCannotInsertNullCreatedAt() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("INSERT INTO staff (first_name, last_name, updated_at) " +
                               "VALUES ('John', 'Doe', CURRENT_TIMESTAMP)");
        });
    }

    @Test
    void testStaffCannotInsertNullUpdatedAt() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("INSERT INTO staff (first_name, last_name, created_at) " +
                               "VALUES ('John', 'Doe', CURRENT_TIMESTAMP)");
        });
    }

    @Test
    void testMigrationVersionAppliedSuccessfully() {
        String sql = "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '4'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertEquals(1, count);
    }

    @Test
    void testAllAuditIndexesCreatedForPerformance() {
        String sql = "SELECT COUNT(*) FROM pg_indexes WHERE indexname LIKE 'idx_%_created_at' OR indexname LIKE 'idx_%_updated_at'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertEquals(6, count);
    }
}