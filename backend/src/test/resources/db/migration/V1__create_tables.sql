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
 * Validates table creation, column definitions, constraints, and indexes
 */
@SpringBootTest
@Transactional
public class V1CreateTablesTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testFacilityTableExists() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "facility", null);
            assertTrue(tables.next(), "facility table should exist");
        }
    }

    @Test
    public void testFacilityTableHasRequiredColumns() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "facility", null);
            
            List<String> columnNames = new ArrayList<>();
            while (columns.next()) {
                columnNames.add(columns.getString("COLUMN_NAME"));
            }
            
            assertTrue(columnNames.contains("id"), "facility table should have id column");
            assertTrue(columnNames.contains("name"), "facility table should have name column");
            assertTrue(columnNames.contains("timezone"), "facility table should have timezone column");
            assertTrue(columnNames.contains("region_code"), "facility table should have region_code column");
            assertTrue(columnNames.contains("address"), "facility table should have address column");
            assertTrue(columnNames.contains("active"), "facility table should have active column");
            assertTrue(columnNames.contains("created_at"), "facility table should have created_at column");
            assertTrue(columnNames.contains("updated_at"), "facility table should have updated_at column");
            assertTrue(columnNames.contains("created_by"), "facility table should have created_by column");
            assertTrue(columnNames.contains("updated_by"), "facility table should have updated_by column");
        }
    }

    @Test
    public void testUserAccountTableExists() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "user_account", null);
            assertTrue(tables.next(), "user_account table should exist");
        }
    }

    @Test
    public void testUserAccountTableHasRequiredColumns() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "user_account", null);
            
            List<String> columnNames = new ArrayList<>();
            while (columns.next()) {
                columnNames.add(columns.getString("COLUMN_NAME"));
            }
            
            assertTrue(columnNames.contains("id"), "user_account table should have id column");
            assertTrue(columnNames.contains("username"), "user_account table should have username column");
            assertTrue(columnNames.contains("roles"), "user_account table should have roles column");
            assertTrue(columnNames.contains("email"), "user_account table should have email column");
            assertTrue(columnNames.contains("password_hash"), "user_account table should have password_hash column");
            assertTrue(columnNames.contains("facility_id"), "user_account table should have facility_id column");
            assertTrue(columnNames.contains("created_at"), "user_account table should have created_at column");
            assertTrue(columnNames.contains("updated_at"), "user_account table should have updated_at column");
            assertTrue(columnNames.contains("created_by"), "user_account table should have created_by column");
            assertTrue(columnNames.contains("updated_by"), "user_account table should have updated_by column");
        }
    }

    @Test
    public void testAuditLogTableExists() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "audit_log", null);
            assertTrue(tables.next(), "audit_log table should exist for storing audit events");
        }
    }

    @Test
    public void testAuditLogTableHasRequiredColumns() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "audit_log", null);
            
            List<String> columnNames = new ArrayList<>();
            while (columns.next()) {
                columnNames.add(columns.getString("COLUMN_NAME"));
            }
            
            assertTrue(columnNames.contains("id"), "audit_log table should have id column");
            assertTrue(columnNames.contains("event_type"), "audit_log table should have event_type column");
            assertTrue(columnNames.contains("entity_type"), "audit_log table should have entity_type column");
            assertTrue(columnNames.contains("entity_id"), "audit_log table should have entity_id column");
            assertTrue(columnNames.contains("user_id"), "audit_log table should have user_id column");
            assertTrue(columnNames.contains("username"), "audit_log table should have username column");
            assertTrue(columnNames.contains("facility_id"), "audit_log table should have facility_id column");
            assertTrue(columnNames.contains("action"), "audit_log table should have action column");
            assertTrue(columnNames.contains("old_value"), "audit_log table should have old_value column");
            assertTrue(columnNames.contains("new_value"), "audit_log table should have new_value column");
            assertTrue(columnNames.contains("ip_address"), "audit_log table should have ip_address column");
            assertTrue(columnNames.contains("user_agent"), "audit_log table should have user_agent column");
            assertTrue(columnNames.contains("timestamp"), "audit_log table should have timestamp column");
        }
    }

    @Test
    public void testForeignKeyConstraintUserAccountToFacility() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet foreignKeys = metaData.getImportedKeys(null, null, "user_account");
            
            boolean foundFacilityFK = false;
            while (foreignKeys.next()) {
                String fkName = foreignKeys.getString("FK_NAME");
                String pkTableName = foreignKeys.getString("PKTABLE_NAME");
                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                
                if ("fk_user_account_facility".equals(fkName) && 
                    "facility".equals(pkTableName) && 
                    "facility_id".equals(fkColumnName)) {
                    foundFacilityFK = true;
                    break;
                }
            }
            
            assertTrue(foundFacilityFK, "Foreign key constraint fk_user_account_facility should exist");
        }
    }

    @Test
    public void testForeignKeyConstraintAuditLogToUserAccount() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet foreignKeys = metaData.getImportedKeys(null, null, "audit_log");
            
            boolean foundUserFK = false;
            while (foreignKeys.next()) {
                String fkName = foreignKeys.getString("FK_NAME");
                String pkTableName = foreignKeys.getString("PKTABLE_NAME");
                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                
                if ("fk_audit_log_user".equals(fkName) && 
                    "user_account".equals(pkTableName) && 
                    "user_id".equals(fkColumnName)) {
                    foundUserFK = true;
                    break;
                }
            }
            
            assertTrue(foundUserFK, "Foreign key constraint fk_audit_log_user should exist");
        }
    }

    @Test
    public void testForeignKeyConstraintAuditLogToFacility() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet foreignKeys = metaData.getImportedKeys(null, null, "audit_log");
            
            boolean foundFacilityFK = false;
            while (foreignKeys.next()) {
                String fkName = foreignKeys.getString("FK_NAME");
                String pkTableName = foreignKeys.getString("PKTABLE_NAME");
                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                
                if ("fk_audit_log_facility".equals(fkName) && 
                    "facility".equals(pkTableName) && 
                    "facility_id".equals(fkColumnName)) {
                    foundFacilityFK = true;
                    break;
                }
            }
            
            assertTrue(foundFacilityFK, "Foreign key constraint fk_audit_log_facility should exist");
        }
    }

    @Test
    public void testUniqueConstraintFacilityUsername() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet indexes = metaData.getIndexInfo(null, null, "user_account", true, false);
            
            boolean foundUniqueConstraint = false;
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                if ("uk_facility_username".equals(indexName)) {
                    foundUniqueConstraint = true;
                    break;
                }
            }
            
            assertTrue(foundUniqueConstraint, "Unique constraint uk_facility_username should exist");
        }
    }

    @Test
    public void testIndexUserAccountFacilityRole() throws SQLException {
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
    public void testIndexAuditLogTimestamp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet indexes = metaData.getIndexInfo(null, null, "audit_log", false, false);
            
            boolean foundIndex = false;
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                if ("idx_audit_log_timestamp".equals(indexName)) {
                    foundIndex = true;
                    break;
                }
            }
            
            assertTrue(foundIndex, "Index idx_audit_log_timestamp should exist");
        }
    }

    @Test
    public void testIndexAuditLogEntity() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet indexes = metaData.getIndexInfo(null, null, "audit_log", false, false);
            
            boolean foundIndex = false;
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                if ("idx_audit_log_entity".equals(indexName)) {
                    foundIndex = true;
                    break;
                }
            }
            
            assertTrue(foundIndex, "Index idx_audit_log_entity should exist");
        }
    }

    @Test
    public void testIndexAuditLogUser() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet indexes = metaData.getIndexInfo(null, null, "audit_log", false, false);
            
            boolean foundIndex = false;
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                if ("idx_audit_log_user".equals(indexName)) {
                    foundIndex = true;
                    break;
                }
            }
            
            assertTrue(foundIndex, "Index idx_audit_log_user should exist");
        }
    }

    @Test
    public void testIndexAuditLogFacility() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet indexes = metaData.getIndexInfo(null, null, "audit_log", false, false);
            
            boolean foundIndex = false;
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                if ("idx_audit_log_facility".equals(indexName)) {
                    foundIndex = true;
                    break;
                }
            }
            
            assertTrue(foundIndex, "Index idx_audit_log_facility should exist");
        }
    }

    @Test
    public void testFacilityPrimaryKeyExists() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, "facility");
            
            boolean foundPK = false;
            while (primaryKeys.next()) {
                String columnName = primaryKeys.getString("COLUMN_NAME");
                if ("id".equals(columnName)) {
                    foundPK = true;
                    break;
                }
            }
            
            assertTrue(foundPK, "facility table should have primary key on id column");
        }
    }

    @Test
    public void testUserAccountPrimaryKeyExists() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, "user_account");
            
            boolean foundPK = false;
            while (primaryKeys.next()) {
                String columnName = primaryKeys.getString("COLUMN_NAME");
                if ("id".equals(columnName)) {
                    foundPK = true;
                    break;
                }
            }
            
            assertTrue(foundPK, "user_account table should have primary key on id column");
        }
    }

    @Test
    public void testAuditLogPrimaryKeyExists() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, "audit_log");
            
            boolean foundPK = false;
            while (primaryKeys.next()) {
                String columnName = primaryKeys.getString("COLUMN_NAME");
                if ("id".equals(columnName)) {
                    foundPK = true;
                    break;
                }
            }
            
            assertTrue(foundPK, "audit_log table should have primary key on id column");
        }
    }

    @Test
    public void testFacilityNotNullConstraints() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "facility", null);
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                int nullable = columns.getInt("NULLABLE");
                
                if ("name".equals(columnName) || "timezone".equals(columnName) || "active".equals(columnName)) {
                    assertEquals(DatabaseMetaData.columnNoNulls, nullable, 
                        columnName + " should be NOT NULL");
                }
            }
        }
    }

    @Test
    public void testUserAccountNotNullConstraints() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "user_account", null);
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                int nullable = columns.getInt("NULLABLE");
                
                if ("username".equals(columnName) || "password_hash".equals(columnName) || 
                    "roles".equals(columnName) || "facility_id".equals(columnName)) {
                    assertEquals(DatabaseMetaData.columnNoNulls, nullable, 
                        columnName + " should be NOT NULL");
                }
            }
        }
    }

    @Test
    public void testAuditLogNotNullConstraints() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "audit_log", null);
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                int nullable = columns.getInt("NULLABLE");
                
                if ("event_type".equals(columnName) || "entity_type".equals(columnName) || 
                    "action".equals(columnName) || "timestamp".equals(columnName)) {
                    assertEquals(DatabaseMetaData.columnNoNulls, nullable, 
                        columnName + " should be NOT NULL");
                }
            }
        }
    }

    @Test
    public void testReferentialIntegrityFacilityToUserAccount() {
        jdbcTemplate.execute("INSERT INTO facility (name, timezone) VALUES ('Test Facility', 'America/New_York')");
        Long facilityId = jdbcTemplate.queryForObject("SELECT id FROM facility WHERE name = 'Test Facility'", Long.class);
        
        jdbcTemplate.execute(String.format(
            "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES ('testuser', 'hash123', 'MANAGER', %d)", 
            facilityId));
        
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_account WHERE facility_id = ?", 
            Long.class, facilityId);
        
        assertEquals(1L, count, "User account should be created with valid facility reference");
    }

    @Test
    public void testReferentialIntegrityAuditLogToUserAccount() {
        jdbcTemplate.execute("INSERT INTO facility (name, timezone) VALUES ('Test Facility', 'America/New_York')");
        Long facilityId = jdbcTemplate.queryForObject("SELECT id FROM facility WHERE name = 'Test Facility'", Long.class);
        
        jdbcTemplate.execute(String.format(
            "INSERT INTO user_account (username, password_hash, roles, facility_id) VALUES ('testuser', 'hash123', 'MANAGER', %d)", 
            facilityId));
        Long userId = jdbcTemplate.queryForObject("SELECT id FROM user_account WHERE username = 'testuser'", Long.class);
        
        jdbcTemplate.execute(String.format(
            "INSERT INTO audit_log (event_type, entity_type, action, user_id, facility_id) VALUES ('LOGIN', 'USER', 'ACCESS', %d, %d)", 
            userId, facilityId));
        
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM audit_log WHERE user_id = ?", 
            Long.class, userId);
        
        assertEquals(1L, count, "Audit log should be created with valid user reference");
    }

    @Test
    public void testFacilityDefaultValues() {
        jdbcTemplate.execute("INSERT INTO facility (name, timezone) VALUES ('Test Facility', 'America/New_York')");
        
        Boolean active = jdbcTemplate.queryForObject(
            "SELECT active FROM facility WHERE name = 'Test Facility'", 
            Boolean.class);
        
        assertTrue(active, "Facility active should default to true");
    }
}