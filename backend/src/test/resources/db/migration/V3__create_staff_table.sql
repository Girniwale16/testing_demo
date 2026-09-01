package com.example.testingdemo.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/db/migration/V3__create_staff_table.sql")
class V3CreateStaffTableMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testStaffTableExists() {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'staff'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testStaffTableHasIdColumn() {
        String sql = "SELECT column_name, data_type, column_key, extra FROM information_schema.columns " +
                     "WHERE table_name = 'staff' AND column_name = 'id'";
        Map<String, Object> column = jdbcTemplate.queryForMap(sql);
        
        assertThat(column.get("column_name")).isEqualTo("id");
        assertThat(column.get("data_type")).isEqualTo("bigint");
        assertThat(column.get("column_key")).isEqualTo("PRI");
        assertThat(column.get("extra")).asString().contains("auto_increment");
    }

    @Test
    void testStaffTableHasFirstNameColumn() {
        String sql = "SELECT column_name, data_type, is_nullable, character_maximum_length FROM information_schema.columns " +
                     "WHERE table_name = 'staff' AND column_name = 'first_name'";
        Map<String, Object> column = jdbcTemplate.queryForMap(sql);
        
        assertThat(column.get("column_name")).isEqualTo("first_name");
        assertThat(column.get("data_type")).isEqualTo("varchar");
        assertThat(column.get("is_nullable")).isEqualTo("NO");
        assertThat(column.get("character_maximum_length")).isEqualTo(255L);
    }

    @Test
    void testStaffTableHasLastNameColumn() {
        String sql = "SELECT column_name, data_type, is_nullable, character_maximum_length FROM information_schema.columns " +
                     "WHERE table_name = 'staff' AND column_name = 'last_name'";
        Map<String, Object> column = jdbcTemplate.queryForMap(sql);
        
        assertThat(column.get("column_name")).isEqualTo("last_name");
        assertThat(column.get("data_type")).isEqualTo("varchar");
        assertThat(column.get("is_nullable")).isEqualTo("NO");
        assertThat(column.get("character_maximum_length")).isEqualTo(255L);
    }

    @Test
    void testStaffTableHasRoleColumn() {
        String sql = "SELECT column_name, data_type, is_nullable, character_maximum_length FROM information_schema.columns " +
                     "WHERE table_name = 'staff' AND column_name = 'role'";
        Map<String, Object> column = jdbcTemplate.queryForMap(sql);
        
        assertThat(column.get("column_name")).isEqualTo("role");
        assertThat(column.get("data_type")).isEqualTo("varchar");
        assertThat(column.get("is_nullable")).isEqualTo("NO");
        assertThat(column.get("character_maximum_length")).isEqualTo(255L);
    }

    @Test
    void testStaffTableHasFacilityNameColumn() {
        String sql = "SELECT column_name, data_type, is_nullable, character_maximum_length FROM information_schema.columns " +
                     "WHERE table_name = 'staff' AND column_name = 'facility_name'";
        Map<String, Object> column = jdbcTemplate.queryForMap(sql);
        
        assertThat(column.get("column_name")).isEqualTo("facility_name");
        assertThat(column.get("data_type")).isEqualTo("varchar");
        assertThat(column.get("is_nullable")).isEqualTo("NO");
        assertThat(column.get("character_maximum_length")).isEqualTo(255L);
    }

    @Test
    void testStaffTableHasIsDeactivatedColumn() {
        String sql = "SELECT column_name, data_type, column_default FROM information_schema.columns " +
                     "WHERE table_name = 'staff' AND column_name = 'is_deactivated'";
        Map<String, Object> column = jdbcTemplate.queryForMap(sql);
        
        assertThat(column.get("column_name")).isEqualTo("is_deactivated");
        assertThat(column.get("data_type")).isEqualTo("tinyint");
        assertThat(column.get("column_default")).asString().contains("0");
    }

    @Test
    void testStaffTableHasCreatedAtColumn() {
        String sql = "SELECT column_name, data_type, column_default FROM information_schema.columns " +
                     "WHERE table_name = 'staff' AND column_name = 'created_at'";
        Map<String, Object> column = jdbcTemplate.queryForMap(sql);
        
        assertThat(column.get("column_name")).isEqualTo("created_at");
        assertThat(column.get("data_type")).isEqualTo("timestamp");
        assertThat(column.get("column_default")).asString().containsIgnoringCase("CURRENT_TIMESTAMP");
    }

    @Test
    void testStaffTableHasUpdatedAtColumn() {
        String sql = "SELECT column_name, data_type, column_default, extra FROM information_schema.columns " +
                     "WHERE table_name = 'staff' AND column_name = 'updated_at'";
        Map<String, Object> column = jdbcTemplate.queryForMap(sql);
        
        assertThat(column.get("column_name")).isEqualTo("updated_at");
        assertThat(column.get("data_type")).isEqualTo("timestamp");
        assertThat(column.get("column_default")).asString().containsIgnoringCase("CURRENT_TIMESTAMP");
        assertThat(column.get("extra")).asString().containsIgnoringCase("on update CURRENT_TIMESTAMP");
    }

    @Test
    void testIndexOnIsDeactivatedExists() {
        String sql = "SELECT index_name, column_name FROM information_schema.statistics " +
                     "WHERE table_name = 'staff' AND index_name = 'idx_staff_deactivated'";
        Map<String, Object> index = jdbcTemplate.queryForMap(sql);
        
        assertThat(index.get("index_name")).isEqualTo("idx_staff_deactivated");
        assertThat(index.get("column_name")).isEqualTo("is_deactivated");
    }

    @Test
    void testIndexOnFacilityNameExists() {
        String sql = "SELECT index_name, column_name FROM information_schema.statistics " +
                     "WHERE table_name = 'staff' AND index_name = 'idx_staff_facility'";
        Map<String, Object> index = jdbcTemplate.queryForMap(sql);
        
        assertThat(index.get("index_name")).isEqualTo("idx_staff_facility");
        assertThat(index.get("column_name")).isEqualTo("facility_name");
    }

    @Test
    void testInsertStaffRecordWithAllRequiredFields() {
        String insertSql = "INSERT INTO staff (first_name, last_name, role, facility_name) " +
                          "VALUES (?, ?, ?, ?)";
        int rowsAffected = jdbcTemplate.update(insertSql, "John", "Doe", "Nurse", "Main Hospital");
        
        assertThat(rowsAffected).isEqualTo(1);
        
        String selectSql = "SELECT * FROM staff WHERE first_name = 'John' AND last_name = 'Doe'";
        Map<String, Object> staff = jdbcTemplate.queryForMap(selectSql);
        
        assertThat(staff.get("first_name")).isEqualTo("John");
        assertThat(staff.get("last_name")).isEqualTo("Doe");
        assertThat(staff.get("role")).isEqualTo("Nurse");
        assertThat(staff.get("facility_name")).isEqualTo("Main Hospital");
        assertThat(staff.get("is_deactivated")).isEqualTo(false);
        assertThat(staff.get("created_at")).isNotNull();
        assertThat(staff.get("updated_at")).isNotNull();
    }

    @Test
    void testIsDeactivatedDefaultsToFalse() {
        String insertSql = "INSERT INTO staff (first_name, last_name, role, facility_name) " +
                          "VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(insertSql, "Jane", "Smith", "Doctor", "East Clinic");
        
        String selectSql = "SELECT is_deactivated FROM staff WHERE first_name = 'Jane'";
        Boolean isDeactivated = jdbcTemplate.queryForObject(selectSql, Boolean.class);
        
        assertThat(isDeactivated).isFalse();
    }

    @Test
    void testCreatedAtAutoPopulates() {
        String insertSql = "INSERT INTO staff (first_name, last_name, role, facility_name) " +
                          "VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(insertSql, "Bob", "Johnson", "Technician", "West Lab");
        
        String selectSql = "SELECT created_at FROM staff WHERE first_name = 'Bob'";
        Object createdAt = jdbcTemplate.queryForObject(selectSql, Object.class);
        
        assertThat(createdAt).isNotNull();
    }

    @Test
    void testUpdatedAtAutoPopulates() {
        String insertSql = "INSERT INTO staff (first_name, last_name, role, facility_name) " +
                          "VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(insertSql, "Alice", "Williams", "Administrator", "North Office");
        
        String selectSql = "SELECT updated_at FROM staff WHERE first_name = 'Alice'";
        Object updatedAt = jdbcTemplate.queryForObject(selectSql, Object.class);
        
        assertThat(updatedAt).isNotNull();
    }

    @Test
    void testQueryByIsDeactivatedUsesIndex() {
        // Insert test data
        jdbcTemplate.update("INSERT INTO staff (first_name, last_name, role, facility_name, is_deactivated) " +
                           "VALUES (?, ?, ?, ?, ?)", "Active", "User", "Nurse", "Hospital A", false);
        jdbcTemplate.update("INSERT INTO staff (first_name, last_name, role, facility_name, is_deactivated) " +
                           "VALUES (?, ?, ?, ?, ?)", "Inactive", "User", "Doctor", "Hospital B", true);
        
        String sql = "SELECT COUNT(*) FROM staff WHERE is_deactivated = false";
        Integer activeCount = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertThat(activeCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testQueryByFacilityNameUsesIndex() {
        // Insert test data
        jdbcTemplate.update("INSERT INTO staff (first_name, last_name, role, facility_name) " +
                           "VALUES (?, ?, ?, ?)", "Staff1", "Member1", "Nurse", "Central Hospital");
        jdbcTemplate.update("INSERT INTO staff (first_name, last_name, role, facility_name) " +
                           "VALUES (?, ?, ?, ?)", "Staff2", "Member2", "Doctor", "Central Hospital");
        
        String sql = "SELECT COUNT(*) FROM staff WHERE facility_name = 'Central Hospital'";
        Integer facilityCount = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertThat(facilityCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    void testStaffTableHasAllRequiredColumns() {
        String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'staff'";
        Integer columnCount = jdbcTemplate.queryForObject(sql, Integer.class);
        
        assertThat(columnCount).isEqualTo(8);
    }

    @Test
    void testPrimaryKeyConstraintOnId() {
        String sql = "SELECT constraint_name FROM information_schema.table_constraints " +
                     "WHERE table_name = 'staff' AND constraint_type = 'PRIMARY KEY'";
        List<String> constraints = jdbcTemplate.queryForList(sql, String.class);
        
        assertThat(constraints).isNotEmpty();
    }
}