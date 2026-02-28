package account;

import account.domain.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Base64;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Credentials for test users (passwords are >= 12 chars, not in breach list)
    private static final String ADMIN_EMAIL = "admin2@acme.com";
    private static final String ADMIN_PASSWORD = "adminpassword123";
    private static final String USER_EMAIL = "user2@acme.com";
    private static final String USER_PASSWORD = "userpassword123";
    private static final String ACCOUNTANT_EMAIL = "acct2@acme.com";
    private static final String ACCOUNTANT_PASSWORD = "acctpassword123";
    private static final String AUDITOR_EMAIL = "audit2@acme.com";
    private static final String AUDITOR_PASSWORD = "auditpassword123";

    private static String basicAuth(String user, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
    }

    // ===================== AUTH CONTROLLER =====================

    @Test
    @Order(1)
    void signup_firstUser_getsAdminRole() throws Exception {
        AddUserDto dto = new AddUserDto("Admin", "User", ADMIN_EMAIL, ADMIN_PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(ADMIN_EMAIL))
                .andExpect(jsonPath("$.roles", hasItem("ROLE_ADMINISTRATOR")));
    }

    @Test
    @Order(2)
    void signup_subsequentUser_getsUserRole() throws Exception {
        AddUserDto dto = new AddUserDto("Regular", "User", USER_EMAIL, USER_PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.roles", hasItem("ROLE_USER")));
    }

    @Test
    @Order(3)
    void signup_accountantUser() throws Exception {
        AddUserDto dto = new AddUserDto("Accountant", "User", ACCOUNTANT_EMAIL, ACCOUNTANT_PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasItem("ROLE_USER")));
    }

    @Test
    @Order(4)
    void signup_auditorUser() throws Exception {
        AddUserDto dto = new AddUserDto("Auditor", "User", AUDITOR_EMAIL, AUDITOR_PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasItem("ROLE_USER")));
    }

    @Test
    @Order(5)
    void signup_existingUser_returns400() throws Exception {
        AddUserDto dto = new AddUserDto("Admin", "User", ADMIN_EMAIL, ADMIN_PASSWORD);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    void signup_invalidEmail_returns400() throws Exception {
        AddUserDto dto = new AddUserDto("Bad", "User", "notanemail", "validpassword123");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    void signup_passwordTooShort_returns400() throws Exception {
        // Missing fields trigger 400 validation error from @NotBlank
        AddUserDto dto = new AddUserDto("Bad", "User", "short@acme.com", "short");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    void changepass_success_returns200() throws Exception {
        ChangePwdDto dto = new ChangePwdDto("newpassword123456");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/changepass")
                        .header("Authorization", basicAuth(USER_EMAIL, USER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.status").value("The password has been updated successfully"));
    }

    @Test
    @Order(9)
    void changepass_unauthenticated_returns401() throws Exception {
        ChangePwdDto dto = new ChangePwdDto("newpassword123456");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/changepass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // ===================== ADMIN CONTROLLER =====================

    @Test
    @Order(10)
    void adminGrantRole_grantAccountant_returns200() throws Exception {
        ChangeUserRoleDto dto = new ChangeUserRoleDto(ACCOUNTANT_EMAIL, "ACCOUNTANT", "GRANT");
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/user/role")
                        .header("Authorization", basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasItem("ROLE_ACCOUNTANT")));
    }

    @Test
    @Order(11)
    void adminGrantRole_grantAuditor_returns200() throws Exception {
        ChangeUserRoleDto dto = new ChangeUserRoleDto(AUDITOR_EMAIL, "AUDITOR", "GRANT");
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/user/role")
                        .header("Authorization", basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasItem("ROLE_AUDITOR")));
    }

    @Test
    @Order(12)
    void adminRemoveRole_success_returns200() throws Exception {
        ChangeUserRoleDto dto = new ChangeUserRoleDto(AUDITOR_EMAIL, "AUDITOR", "REMOVE");
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/user/role")
                        .header("Authorization", basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", not(hasItem("ROLE_AUDITOR"))));
    }

    @Test
    @Order(13)
    void adminGrantRole_invalidOperation_returns400() throws Exception {
        // operation must match GRANT|REMOVE
        String body = "{\"user\":\"" + USER_EMAIL + "\",\"role\":\"USER\",\"operation\":\"INVALID\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/user/role")
                        .header("Authorization", basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(14)
    void adminGrantRole_unauthorizedUser_returns403() throws Exception {
        ChangeUserRoleDto dto = new ChangeUserRoleDto(USER_EMAIL, "ACCOUNTANT", "GRANT");
        // After changepass in order 8, USER_EMAIL password is now "newpassword123456"
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/user/role")
                        .header("Authorization", basicAuth(USER_EMAIL, "newpassword123456"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(15)
    void adminLockUser_success_returns200() throws Exception {
        ChangeUserLockDto dto = new ChangeUserLockDto(USER_EMAIL, "LOCK");
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/user/access")
                        .header("Authorization", basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("User " + USER_EMAIL + " locked!"));
    }

    @Test
    @Order(16)
    void adminUnlockUser_success_returns200() throws Exception {
        ChangeUserLockDto dto = new ChangeUserLockDto(USER_EMAIL, "UNLOCK");
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/user/access")
                        .header("Authorization", basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("User " + USER_EMAIL + " unlocked!"));
    }

    @Test
    @Order(17)
    void adminChangeAccess_invalidOperation_returns400() throws Exception {
        String body = "{\"user\":\"" + USER_EMAIL + "\",\"operation\":\"INVALID\"}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/user/access")
                        .header("Authorization", basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(18)
    void adminGetAllUsers_returns200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/user/")
                        .header("Authorization", basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(19)
    void adminGetAllUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/user/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(20)
    void adminDeleteUser_success_returns200() throws Exception {
        // Create a temporary user to delete
        AddUserDto dto = new AddUserDto("Temp", "User", "temp2@acme.com", "temppassword123");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/user/temp2@acme.com")
                        .header("Authorization", basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value("temp2@acme.com"))
                .andExpect(jsonPath("$.status").value("Deleted successfully!"));
    }

    // ===================== ACCOUNTANT CONTROLLER =====================

    @Test
    @Order(21)
    void acctUploadPayrolls_success_returns200() throws Exception {
        // Use raw JSON with "MM-YYYY" period format required by YearMonthDeserializer
        String body = "[{\"employee\":\"" + USER_EMAIL + "\",\"period\":\"01-2023\",\"salary\":200000}]";
        mockMvc.perform(MockMvcRequestBuilders.post("/api/acct/payments")
                        .header("Authorization", basicAuth(ACCOUNTANT_EMAIL, ACCOUNTANT_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Added successfully!"));
    }

    @Test
    @Order(22)
    void acctUpdateSalary_success_returns200() throws Exception {
        // Use raw JSON with "MM-YYYY" period format required by YearMonthDeserializer
        String body = "{\"employee\":\"" + USER_EMAIL + "\",\"period\":\"01-2023\",\"salary\":300000}";
        mockMvc.perform(MockMvcRequestBuilders.put("/api/acct/payments")
                        .header("Authorization", basicAuth(ACCOUNTANT_EMAIL, ACCOUNTANT_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Updated successfully!"));
    }

    @Test
    @Order(23)
    void acctUploadPayrolls_unauthenticated_returns401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/acct/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isUnauthorized());
    }

    // ===================== EMPLOYEE CONTROLLER =====================

    @Test
    @Order(24)
    void employeeGetPayrollForPeriod_returns200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/empl/payment")
                        .header("Authorization", basicAuth(USER_EMAIL, "newpassword123456"))
                        .param("period", "01-2023"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salary").value("3000 dollar(s) 0 cent(s)"));
    }

    @Test
    @Order(25)
    void employeeGetAllPayrolls_returns200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/empl/payment")
                        .header("Authorization", basicAuth(USER_EMAIL, "newpassword123456")))
                .andExpect(status().isOk());
    }

    @Test
    @Order(26)
    void employeeGetPayroll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/empl/payment"))
                .andExpect(status().isUnauthorized());
    }

    // ===================== SECURITY CONTROLLER =====================

    @Test
    @Order(27)
    void securityGetEvents_withAuditor_returns200() throws Exception {
        // Grant auditor role first
        ChangeUserRoleDto dto = new ChangeUserRoleDto(AUDITOR_EMAIL, "AUDITOR", "GRANT");
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/user/role")
                        .header("Authorization", basicAuth(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/security/events/")
                        .header("Authorization", basicAuth(AUDITOR_EMAIL, AUDITOR_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", instanceOf(List.class)));
    }

    @Test
    @Order(28)
    void securityGetEvents_unauthenticated_returns401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/security/events/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(29)
    void securityGetEvents_withRegularUser_returns403() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/security/events/")
                        .header("Authorization", basicAuth(USER_EMAIL, "newpassword123456")))
                .andExpect(status().isForbidden());
    }
}
