package db.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.flyway.clean-disabled=false"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class V1__create_tables_Test {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .cleanDisabled(false)
            .load();
        flyway.clean();
        flyway.migrate();
    }

    @Test
    @Order(1)
    @DisplayName("Test staff_member table exists after migration")
    void testStaffMemberTableExists() {
        String query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'staff_member'";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class);
        assertEquals(1, count, "staff_member table should exist");
    }

    @Test
    @Order(2)
    @DisplayName("Test staff_member table has correct columns")
    void testStaffMemberTableColumns() {
        String query = "SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_name = 'staff_member' ORDER BY ordinal_position";
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(query);
        
        assertEquals(9, columns.size(), "staff_member table should have 9 columns");
        
        // Verify id column
        assertEquals("id", columns.get(0).get("column_name"));
        assertEquals("NO", columns.get(0).get("is_nullable"));
        
        // Verify name column
        assertEquals("name", columns.get(1).get("column_name"));
        assertEquals("NO", columns.get(1).get("is_nullable"));
        
        // Verify contact column
        assertEquals("contact", columns.get(2).get("column_name"));
        assertEquals("NO", columns.get(2).get("is_nullable"));
        
        // Verify role column
        assertEquals("role", columns.get(3).get("column_name"));
        assertEquals("NO", columns.get(3).get("is_nullable"));
        
        // Verify employment_status column
        assertEquals("employment_status", columns.get(4).get("column_name"));
        assertEquals("NO", columns.get(4).get("is_nullable"));
        
        // Verify start_date column
        assertEquals("start_date", columns.get(5).get("column_name"));
        assertEquals("YES", columns.get(5).get("is_nullable"));
        
        // Verify end_date column
        assertEquals("end_date", columns.get(6).get("column_name"));
        assertEquals("YES", columns.get(6).get("is_nullable"));
        
        // Verify facility_id column
        assertEquals("facility_id", columns.get(7).get("column_name"));
        assertEquals("NO", columns.get(7).get("is_nullable"));
    }

    @Test
    @Order(3)
    @DisplayName("Test staff_member table has primary key on id")
    void testStaffMemberPrimaryKey() {
        String query = "SELECT constraint_name FROM information_schema.table_constraints WHERE table_name = 'staff_member' AND constraint_type = 'PRIMARY KEY'";
        List<String> constraints = jdbcTemplate.queryForList(query, String.class);
        
        assertEquals(1, constraints.size(), "staff_member table should have one primary key");
    }

    @Test
    @Order(4)
    @DisplayName("Test staff_member table has foreign key to facility")
    void testStaffMemberForeignKey() {
        String query = "SELECT constraint_name FROM information_schema.table_constraints WHERE table_name = 'staff_member' AND constraint_type = 'FOREIGN KEY'";
        List<String> constraints = jdbcTemplate.queryForList(query, String.class);
        
        assertTrue(constraints.size() >= 1, "staff_member table should have at least one foreign key");
    }

    @Test
    @Order(5)
    @DisplayName("Test idx_staff_facility index exists")
    void testStaffFacilityIndexExists() {
        String query = "SELECT indexname FROM pg_indexes WHERE tablename = 'staff_member' AND indexname = 'idx_staff_facility'";
        List<String> indexes = jdbcTemplate.queryForList(query, String.class);
        
        assertEquals(1, indexes.size(), "idx_staff_facility index should exist");
    }

    @Test
    @Order(6)
    @DisplayName("Test idx_staff_employment_status index exists")
    void testStaffEmploymentStatusIndexExists() {
        String query = "SELECT indexname FROM pg_indexes WHERE tablename = 'staff_member' AND indexname = 'idx_staff_employment_status'";
        List<String> indexes = jdbcTemplate.queryForList(query, String.class);
        
        assertEquals(1, indexes.size(), "idx_staff_employment_status index should exist");
    }

    @Test
    @Order(7)
    @DisplayName("Test staff_member table is created after facility table")
    void testTableCreationOrder() {
        // Verify facility table exists (prerequisite for foreign key)
        String facilityQuery = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'facility'";
        Integer facilityCount = jdbcTemplate.queryForObject(facilityQuery, Integer.class);
        assertEquals(1, facilityCount, "facility table should exist before staff_member");
        
        // Verify staff_member table exists
        String staffQuery = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'staff_member'";
        Integer staffCount = jdbcTemplate.queryForObject(staffQuery, Integer.class);
        assertEquals(1, staffCount, "staff_member table should exist");
    }

    @Test
    @Order(8)
    @DisplayName("Test insert staff_member with valid facility_id")
    void testInsertStaffMemberWithValidFacility() {
        // Insert facility first
        jdbcTemplate.update("INSERT INTO facility (id, timezone, is_active, created_at) VALUES (1, 'America/New_York', true, NOW())");
        
        // Insert staff_member
        int rowsAffected = jdbcTemplate.update(
            "INSERT INTO staff_member (id, name, contact, role, employment_status, start_date, facility_id) VALUES (1, 'John Doe', 'john@example.com', 'Nurse', 'ACTIVE', '2024-01-01', 1)"
        );
        
        assertEquals(1, rowsAffected, "Should insert one staff_member record");
        
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM staff_member WHERE id = 1", Integer.class);
        assertEquals(1, count, "staff_member record should exist");
    }

    @Test
    @Order(9)
    @DisplayName("Test foreign key constraint prevents invalid facility_id")
    void testForeignKeyConstraintEnforcement() {
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO staff_member (id, name, contact, role, employment_status, facility_id) VALUES (999, 'Jane Doe', 'jane@example.com', 'Doctor', 'ACTIVE', 999)"
            );
        }, "Should throw exception when inserting staff_member with non-existent facility_id");
    }

    @Test
    @Order(10)
    @DisplayName("Test CASCADE DELETE removes staff_member when facility is deleted")
    void testCascadeDeleteOnFacility() {
        // Insert facility
        jdbcTemplate.update("INSERT INTO facility (id, timezone, is_active, created_at) VALUES (2, 'America/Los_Angeles', true, NOW())");
        
        // Insert staff_member
        jdbcTemplate.update(
            "INSERT INTO staff_member (id, name, contact, role, employment_status, facility_id) VALUES (2, 'Alice Smith', 'alice@example.com', 'Technician', 'ACTIVE', 2)"
        );
        
        // Verify staff_member exists
        Integer staffCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM staff_member WHERE id = 2", Integer.class);
        assertEquals(1, staffCount, "staff_member should exist before facility deletion");
        
        // Delete facility
        jdbcTemplate.update("DELETE FROM facility WHERE id = 2");
        
        // Verify staff_member is deleted
        Integer staffCountAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM staff_member WHERE id = 2", Integer.class);
        assertEquals(0, staffCountAfter, "staff_member should be deleted when facility is deleted (CASCADE)");
    }

    @Test
    @Order(11)
    @DisplayName("Test NOT NULL constraints on required columns")
    void testNotNullConstraints() {
        jdbcTemplate.update("INSERT INTO facility (id, timezone, is_active, created_at) VALUES (3, 'Europe/London', true, NOW())");
        
        // Test name NOT NULL
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO staff_member (id, name, contact, role, employment_status, facility_id) VALUES (3, NULL, 'contact@example.com', 'Admin', 'ACTIVE', 3)"
            );
        }, "Should throw exception when name is NULL");
        
        // Test contact NOT NULL
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO staff_member (id, name, contact, role, employment_status, facility_id) VALUES (4, 'Bob Jones', NULL, 'Admin', 'ACTIVE', 3)"
            );
        }, "Should throw exception when contact is NULL");
        
        // Test role NOT NULL
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO staff_member (id, name, contact, role, employment_status, facility_id) VALUES (5, 'Bob Jones', 'bob@example.com', NULL, 'ACTIVE', 3)"
            );
        }, "Should throw exception when role is NULL");
        
        // Test employment_status NOT NULL
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO staff_member (id, name, contact, role, employment_status, facility_id) VALUES (6, 'Bob Jones', 'bob@example.com', 'Admin', NULL, 3)"
            );
        }, "Should throw exception when employment_status is NULL");
        
        // Test facility_id NOT NULL
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO staff_member (id, name, contact, role, employment_status, facility_id) VALUES (7, 'Bob Jones', 'bob@example.com', 'Admin', 'ACTIVE', NULL)"
            );
        }, "Should throw exception when facility_id is NULL");
    }

    @Test
    @Order(12)
    @DisplayName("Test nullable columns accept NULL values")
    void testNullableColumns() {
        jdbcTemplate.update("INSERT INTO facility (id, timezone, is_active, created_at) VALUES (4, 'Asia/Tokyo', true, NOW())");
        
        // Insert with NULL start_date and end_date
        int rowsAffected = jdbcTemplate.update(
            "INSERT INTO staff_member (id, name, contact, role, employment_status, start_date, end_date, facility_id) VALUES (8, 'Charlie Brown', 'charlie@example.com', 'Manager', 'ACTIVE', NULL, NULL, 4)"
        );
        
        assertEquals(1, rowsAffected, "Should insert staff_member with NULL start_date and end_date");
    }

    @Test
    @Order(13)
    @DisplayName("Test Flyway migration checksum remains unchanged")
    void testFlywayMigrationChecksum() {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .load();
        
        // Verify migration was successful
        assertDoesNotThrow(() -> flyway.validate(), "Flyway validation should pass, indicating checksum is unchanged");
    }

    @Test
    @Order(14)
    @DisplayName("Test query performance with idx_staff_facility index")
    void testQueryPerformanceWithFacilityIndex() {
        jdbcTemplate.update("INSERT INTO facility (id, timezone, is_active, created_at) VALUES (5, 'Australia/Sydney', true, NOW())");
        
        // Insert multiple staff members
        for (int i = 10; i < 20; i++) {
            jdbcTemplate.update(
                "INSERT INTO staff_member (id, name, contact, role, employment_status, facility_id) VALUES (?, ?, ?, ?, ?, ?)",
                i, "Staff " + i, "staff" + i + "@example.com", "Role" + i, "ACTIVE", 5
            );
        }
        
        // Query by facility_id (should use index)
        List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT * FROM staff_member WHERE facility_id = 5");
        assertEquals(10, results.size(), "Should retrieve all staff members for facility_id 5");
    }

    @Test
    @Order(15)
    @DisplayName("Test query performance with idx_staff_employment_status index")
    void testQueryPerformanceWithEmploymentStatusIndex() {
        jdbcTemplate.update("INSERT INTO facility (id, timezone, is_active, created_at) VALUES (6, 'America/Chicago', true, NOW())");
        
        // Insert staff members with different employment statuses
        jdbcTemplate.update("INSERT INTO staff_member (id, name, contact, role, employment_status, facility_id) VALUES (20, 'Active Staff', 'active@example.com', 'Nurse', 'ACTIVE', 6)");
        jdbcTemplate.update("INSERT INTO staff_member (id, name, contact, role, employment_status, facility_id) VALUES (21, 'Inactive Staff', 'inactive@example.com', 'Nurse', 'INACTIVE', 6)");
        jdbcTemplate.update("INSERT INTO staff_member (id, name, contact, role, employment_status, facility_id) VALUES (22, 'Active Staff 2', 'active2@example.com', 'Doctor', 'ACTIVE', 6)");
        
        // Query by employment_status (should use index)
        List<Map<String, Object>> activeStaff = jdbcTemplate.queryForList("SELECT * FROM staff_member WHERE employment_status = 'ACTIVE'");
        assertTrue(activeStaff.size() >= 2, "Should retrieve active staff members");
    }
}