package com.visionary.roster.controller;

import com.visionary.roster.dto.CreateStaffRequest;
import com.visionary.roster.dto.StaffResponse;
import com.visionary.roster.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for managing staff resources.
 * 
 * <p>This controller provides endpoints for staff management operations including
 * creating new staff members and retrieving staff lists with filtering capabilities.</p>
 * 
 * <p><b>Endpoints:</b></p>
 * <ul>
 *   <li>POST /api/v1/staff - Creates a new staff member (manager-only access)</li>
 *   <li>GET /api/v1/staff - Lists staff members filtered by employment status (default: 'active')</li>
 * </ul>
 * 
 * <p><b>Error Handling:</b></p>
 * <p>All error responses follow the standardized error envelope pattern (Section 9.6)
 * and are handled by the GlobalExceptionHandler:</p>
 * <ul>
 *   <li>400/422 - Validation errors (MethodArgumentNotValidException from @Valid failures)</li>
 *   <li>400 - Invalid date format (IllegalArgumentException from date validation)</li>
 *   <li>403 - Forbidden access (ForbiddenAccessException from roleAuthorizationService)</li>
 * </ul>
 * 
 * @see com.visionary.roster.service.StaffService
 * @see com.visionary.roster.exception.GlobalExceptionHandler
 */
@RestController
@RequestMapping("/api/v1/staff")
@CrossOrigin
public class StaffController {

    private final StaffService staffService;

    /**
     * Constructs a new StaffController with the required service dependency.
     * 
     * @param staffService the staff service for business logic operations
     */
    @Autowired
    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    /**
     * Creates a new staff member.
     * 
     * <p>This endpoint is restricted to manager-only access. Authorization is enforced
     * by the roleAuthorizationService within the service layer.</p>
     * 
     * <p><b>Request Validation:</b></p>
     * <ul>
     *   <li>All required fields must be present and valid (enforced by @Valid)</li>
     *   <li>Date fields must follow the correct format</li>
     *   <li>User must have manager role</li>
     * </ul>
     * 
     * <p><b>Success Response:</b></p>
     * <ul>
     *   <li>Status: 201 Created</li>
     *   <li>Body: StaffResponse containing the created staff details</li>
     * </ul>
     * 
     * <p><b>Error Responses (handled by GlobalExceptionHandler):</b></p>
     * <ul>
     *   <li>400/422 - Validation errors from @Valid annotation</li>
     *   <li>400 - Invalid date format</li>
     *   <li>403 - Forbidden access (non-manager user)</li>
     * </ul>
     * 
     * @param request the staff creation request containing all required staff details
     * @return ResponseEntity with 201 status and the created StaffResponse
     */
    @PostMapping
    public ResponseEntity<StaffResponse> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        StaffResponse response = staffService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists staff members filtered by employment status.
     * 
     * <p>This endpoint retrieves a list of staff members based on their employment status.
     * If no status is specified, it defaults to 'active' to show only currently employed staff.</p>
     * 
     * <p><b>Query Parameters:</b></p>
     * <ul>
     *   <li>employmentStatus (optional, default: "active") - Filter staff by employment status</li>
     * </ul>
     * 
     * <p><b>Success Response:</b></p>
     * <ul>
     *   <li>Status: 200 OK</li>
     *   <li>Body: List of StaffResponse objects matching the filter criteria</li>
     * </ul>
     * 
     * <p><b>Error Responses (handled by GlobalExceptionHandler):</b></p>
     * <ul>
     *   <li>403 - Forbidden access (insufficient permissions)</li>
     * </ul>
     * 
     * @param employmentStatus the employment status filter (default: "active")
     * @return ResponseEntity with 200 status and list of StaffResponse objects
     */
    @GetMapping
    public ResponseEntity<List<StaffResponse>> listStaff(
            @RequestParam(required = false, defaultValue = "active") String employmentStatus) {
        List<StaffResponse> response = staffService.listStaff(employmentStatus);
        return ResponseEntity.ok(response);
    }
}