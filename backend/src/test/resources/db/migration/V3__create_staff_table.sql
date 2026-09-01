package db.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.flyway.clean-disabled=false"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class V3__create_staff_table_Test {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void setUp() throws SQLException {
        flyway.clean();
        flyway.migrate();
        
        // Create test facility for foreign key constraint
        jdbcTemplate.execute("INSERT INTO facility (id, name, address, city, state, zip_code) " +
                           "VALUES (1, 'Test Facility', '123 Test St', 'Test City', 'TS', '12345')");
    }

    @Test
    @Order(1)
    @DisplayName("Test staff table creation with all required columns")
    void testStaffTableCreation() {
        String sql = "SELECT column_name, data_type, is_nullable, column_default " +
                    "FROM information_schema.columns " +
                    "WHERE table_name = 'staff' " +
                    "ORDER BY ordinal_position";
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertFalse(columns.isEmpty(), "Staff table should exist");
        assertTrue(columns.size() >= 13, "Staff table should have at least 13 columns");
        
        // Verify specific columns exist
        assertTrue(columns.stream().anyMatch(c -> "id".equals(c.get("column_name"))));
        assertTrue(columns.stream().anyMatch(c -> "first_name".equals(c.get("column_name"))));
        assertTrue(columns.stream().anyMatch(c -> "last_name".equals(c.get("column_name"))));
        assertTrue(columns.stream().anyMatch(c -> "email".equals(c.get("column_name"))));
        assertTrue(columns.stream().anyMatch(c -> "role".equals(c.get("column_name"))));
        assertTrue(columns.stream().anyMatch(c -> "employment_status".equals(c.get("column_name"))));
        assertTrue(columns.stream().anyMatch(c -> "active".equals(c.get("column_name"))));
        assertTrue(columns.stream().anyMatch(c -> "end_date".equals(c.get("column_name"))));
        assertTrue(columns.stream().anyMatch(c -> "facility_id".equals(c.get("column_name"))));
        assertTrue(columns.stream().anyMatch(c -> "created_at".equals(c.get("column_name"))));
        assertTrue(columns.stream().anyMatch(c -> "updated_at".equals(c.get("column_name"))));
        assertTrue(columns.stream().anyMatch(c -> "created_by".equals(c.get("column_name"))));
        assertTrue(columns.stream().anyMatch(c -> "updated_by".equals(c.get("column_name"))));
    }

    @Test
    @Order(2)
    @DisplayName("Test active column exists with BOOLEAN type and default TRUE")
    void testActiveColumnWithDefaultTrue() {
        String sql = "SELECT column_name, data_type, column_default, is_nullable " +
                    "FROM information_schema.columns " +
                    "WHERE table_name = 'staff' AND column_name = 'active'";
        
        Map<String, Object> column = jdbcTemplate.queryForMap(sql);
        
        assertEquals("active", column.get("column_name"));
        assertEquals("boolean", column.get("data_type"));
        assertEquals("NO", column.get("is_nullable"));
        assertTrue(column.get("column_default").toString().contains("true"));
    }

    @Test
    @Order(3)
    @DisplayName("Test facility_id column with foreign key constraint")
    void testFacilityIdForeignKeyConstraint() {
        String sql = "SELECT constraint_name, table_name, column_name " +
                    "FROM information_schema.key_column_usage " +
                    "WHERE constraint_name = 'fk_staff_facility'";
        
        List<Map<String, Object>> constraints = jdbcTemplate.queryForList(sql);
        
        assertFalse(constraints.isEmpty(), "Foreign key constraint fk_staff_facility should exist");
        assertEquals("staff", constraints.get(0).get("table_name"));
        assertEquals("facility_id", constraints.get(0).get("column_name"));
    }

    @Test
    @Order(4)
    @DisplayName("Test foreign key constraint prevents deletion of referenced facility")
    void testForeignKeyConstraintOnDelete() {
        jdbcTemplate.execute("INSERT INTO staff (first_name, last_name, email, role, facility_id) " +
                           "VALUES ('John', 'Doe', 'john.doe@test.com', 'NURSE', 1)");
        
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("DELETE FROM facility WHERE id = 1");
        }, "Should not allow deletion of facility with associated staff");
    }

    @Test
    @Order(5)
    @DisplayName("Test created_at and updated_at timestamp columns with defaults")
    void testTimestampColumns() {
        String sql = "SELECT column_name, data_type, column_default, is_nullable " +
                    "FROM information_schema.columns " +
                    "WHERE table_name = 'staff' AND column_name IN ('created_at', 'updated_at')";
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertEquals(2, columns.size());
        
        for (Map<String, Object> column : columns) {
            assertTrue(column.get("data_type").toString().contains("timestamp"));
            assertEquals("NO", column.get("is_nullable"));
            assertTrue(column.get("column_default").toString().toLowerCase().contains("current_timestamp") ||
                      column.get("column_default").toString().toLowerCase().contains("now()"));
        }
    }

    @Test
    @Order(6)
    @DisplayName("Test created_by and updated_by audit columns exist")
    void testAuditColumns() {
        String sql = "SELECT column_name, data_type, is_nullable " +
                    "FROM information_schema.columns " +
                    "WHERE table_name = 'staff' AND column_name IN ('created_by', 'updated_by')";
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertEquals(2, columns.size());
        
        for (Map<String, Object> column : columns) {
            assertTrue(column.get("data_type").toString().contains("character varying"));
            assertEquals("YES", column.get("is_nullable"));
        }
    }

    @Test
    @Order(7)
    @DisplayName("Test index on active column exists")
    void testIndexOnActiveColumn() {
        String sql = "SELECT indexname, indexdef " +
                    "FROM pg_indexes " +
                    "WHERE tablename = 'staff' AND indexname = 'idx_staff_active'";
        
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(sql);
        
        assertFalse(indexes.isEmpty(), "Index idx_staff_active should exist");
        assertTrue(indexes.get(0).get("indexdef").toString().contains("active"));
    }

    @Test
    @Order(8)
    @DisplayName("Test composite index on facility_id and active exists")
    void testCompositeIndexFacilityActive() {
        String sql = "SELECT indexname, indexdef " +
                    "FROM pg_indexes " +
                    "WHERE tablename = 'staff' AND indexname = 'idx_staff_facility_active'";
        
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(sql);
        
        assertFalse(indexes.isEmpty(), "Index idx_staff_facility_active should exist");
        String indexDef = indexes.get(0).get("indexdef").toString();
        assertTrue(indexDef.contains("facility_id"));
        assertTrue(indexDef.contains("active"));
    }

    @Test
    @Order(9)
    @DisplayName("Test migration is idempotent - can run multiple times")
    void testMigrationIdempotency() {
        assertDoesNotThrow(() -> {
            flyway.migrate();
            flyway.migrate();
        }, "Migration should be idempotent and not fail on repeated execution");
    }

    @Test
    @Order(10)
    @DisplayName("Test staff insertion with all required fields")
    void testStaffInsertionWithRequiredFields() {
        String sql = "INSERT INTO staff (first_name, last_name, email, role, facility_id, created_by, updated_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        int rowsAffected = jdbcTemplate.update(sql, "Jane", "Smith", "jane.smith@test.com", 
                                              "DOCTOR", 1, "admin", "admin");
        
        assertEquals(1, rowsAffected);
        
        Map<String, Object> staff = jdbcTemplate.queryForMap(
            "SELECT * FROM staff WHERE email = 'jane.smith@test.com'");
        
        assertEquals("Jane", staff.get("first_name"));
        assertEquals("Smith", staff.get("last_name"));
        assertEquals("DOCTOR", staff.get("role"));
        assertEquals(true, staff.get("active"));
        assertEquals("ACTIVE", staff.get("employment_status"));
        assertNotNull(staff.get("created_at"));
        assertNotNull(staff.get("updated_at"));
    }

    @Test
    @Order(11)
    @DisplayName("Test active column default value is TRUE")
    void testActiveColumnDefaultValue() {
        jdbcTemplate.execute("INSERT INTO staff (first_name, last_name, email, role, facility_id) " +
                           "VALUES ('Test', 'User', 'test.user@test.com', 'ADMIN', 1)");
        
        Boolean active = jdbcTemplate.queryForObject(
            "SELECT active FROM staff WHERE email = 'test.user@test.com'", Boolean.class);
        
        assertTrue(active, "Active column should default to TRUE");
    }

    @Test
    @Order(12)
    @DisplayName("Test employment_status check constraint allows only ACTIVE or INACTIVE")
    void testEmploymentStatusCheckConstraint() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("INSERT INTO staff (first_name, last_name, email, role, employment_status, facility_id) " +
                               "VALUES ('Invalid', 'Status', 'invalid@test.com', 'NURSE', 'TERMINATED', 1)");
        }, "Should not allow employment_status other than ACTIVE or INACTIVE");
    }

    @Test
    @Order(13)
    @DisplayName("Test end_date constraint - must be NULL when ACTIVE")
    void testEndDateConstraintForActiveStatus() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("INSERT INTO staff (first_name, last_name, email, role, employment_status, end_date, facility_id) " +
                               "VALUES ('Active', 'WithEndDate', 'active.enddate@test.com', 'NURSE', 'ACTIVE', '2024-01-01', 1)");
        }, "Should not allow end_date when employment_status is ACTIVE");
    }

    @Test
    @Order(14)
    @DisplayName("Test end_date constraint - must be NOT NULL when INACTIVE")
    void testEndDateConstraintForInactiveStatus() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("INSERT INTO staff (first_name, last_name, email, role, employment_status, facility_id) " +
                               "VALUES ('Inactive', 'NoEndDate', 'inactive.noenddate@test.com', 'NURSE', 'INACTIVE', 1)");
        }, "Should not allow NULL end_date when employment_status is INACTIVE");
    }

    @Test
    @Order(15)
    @DisplayName("Test valid INACTIVE staff with end_date")
    void testValidInactiveStaffWithEndDate() {
        String sql = "INSERT INTO staff (first_name, last_name, email, role, employment_status, end_date, facility_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        int rowsAffected = jdbcTemplate.update(sql, "Inactive", "Staff", "inactive.staff@test.com", 
                                              "NURSE", "INACTIVE", LocalDate.of(2024, 1, 1), 1);
        
        assertEquals(1, rowsAffected);
        
        Map<String, Object> staff = jdbcTemplate.queryForMap(
            "SELECT * FROM staff WHERE email = 'inactive.staff@test.com'");
        
        assertEquals("INACTIVE", staff.get("employment_status"));
        assertNotNull(staff.get("end_date"));
    }

    @Test
    @Order(16)
    @DisplayName("Test index on facility_id exists")
    void testIndexOnFacilityId() {
        String sql = "SELECT indexname, indexdef " +
                    "FROM pg_indexes " +
                    "WHERE tablename = 'staff' AND indexname = 'idx_staff_facility_id'";
        
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(sql);
        
        assertFalse(indexes.isEmpty(), "Index idx_staff_facility_id should exist");
        assertTrue(indexes.get(0).get("indexdef").toString().contains("facility_id"));
    }

    @Test
    @Order(17)
    @DisplayName("Test index on employment_status exists")
    void testIndexOnEmploymentStatus() {
        String sql = "SELECT indexname, indexdef " +
                    "FROM pg_indexes " +
                    "WHERE tablename = 'staff' AND indexname = 'idx_staff_employment_status'";
        
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(sql);
        
        assertFalse(indexes.isEmpty(), "Index idx_staff_employment_status should exist");
        assertTrue(indexes.get(0).get("indexdef").toString().contains("employment_status"));
    }

    @Test
    @Order(18)
    @DisplayName("Test composite index on facility_id and employment_status exists")
    void testCompositeIndexFacilityStatus() {
        String sql = "SELECT indexname, indexdef " +
                    "FROM pg_indexes " +
                    "WHERE tablename = 'staff' AND indexname = 'idx_staff_facility_status'";
        
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(sql);
        
        assertFalse(indexes.isEmpty(), "Index idx_staff_facility_status should exist");
        String indexDef = indexes.get(0).get("indexdef").toString();
        assertTrue(indexDef.contains("facility_id"));
        assertTrue(indexDef.contains("employment_status"));
    }

    @Test
    @Order(19)
    @DisplayName("Test email unique constraint")
    void testEmailUniqueConstraint() {
        jdbcTemplate.execute("INSERT INTO staff (first_name, last_name, email, role, facility_id) " +
                           "VALUES ('First', 'User', 'unique@test.com', 'NURSE', 1)");
        
        assertThrows(Exception.class, () -> {
            jdbcTemplate.execute("INSERT INTO staff (first_name, last_name, email, role, facility_id) " +
                               "VALUES ('Second', 'User', 'unique@test.com', 'DOCTOR', 1)");
        }, "Should not allow duplicate email addresses");
    }

    @Test
    @Order(20)
    @DisplayName("Test migration rollback - verify table can be dropped")
    void testMigrationRollback() {
        assertDoesNotThrow(() -> {
            jdbcTemplate.execute("DROP TABLE IF EXISTS staff CASCADE");
        }, "Should be able to drop staff table for rollback");
        
        Integer tableCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'staff'", 
            Integer.class);
        
        assertEquals(0, tableCount, "Staff table should not exist after rollback");
    }
}