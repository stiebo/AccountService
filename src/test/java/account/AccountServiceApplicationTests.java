package account;

import account.domain.dto.AddUserDto;
import account.domain.dto.UserResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountServiceApplicationTests {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    public AccountServiceApplicationTests(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void contextLoads() {
    }

    @Test
    public void testThatCreateAdminUserSuccessfullyReturnsHttp200OkAndCorrectJsonWithROLE_ADMINISTRATOR() throws Exception {
        AddUserDto adminDto = new AddUserDto("Peter", "Pan", "admin@acme.com",
                "thesupersecretpassword");
        String adminDtoJson = objectMapper.writeValueAsString(adminDto);

        UserResponseDto responseDto = new UserResponseDto(1L, "Peter", "Pan",
                "admin@acme.com", List.of("ROLE_ADMINISTRATOR"));
        String expectedJson = objectMapper.writeValueAsString(responseDto);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(adminDtoJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson))
                .andExpect(jsonPath("$.email").value("admin@acme.com")
                );
    }


    @Test
    public void testThatCreateAnotherUserSuccessfullyReturnsHttp200OkAndCorrectJsonWithROLE_USER() throws Exception {
        AddUserDto adminDto = new AddUserDto("Peter", "Pan", "user1@acme.com",
                "thesupersecretpassword");
        String adminDtoJson = objectMapper.writeValueAsString(adminDto);

        UserResponseDto responseDto = new UserResponseDto(2L, "Peter", "Pan",
                "user1@acme.com", List.of("ROLE_USER"));
        String expectedJson = objectMapper.writeValueAsString(responseDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminDtoJson)
        ).andExpect(status().isOk()
        ).andExpect(content().json(expectedJson));
    }


    @Test
    public void testCreateAccountantUserSuccessfullyReturnsHttp200OkAndCorrectJsonWithROLE_USER() throws Exception {
        AddUserDto adminDto = new AddUserDto("Peter", "Pan", "accountant@acme.com",
                "thesupersecretpassword");
        String adminDtoJson = objectMapper.writeValueAsString(adminDto);

        UserResponseDto responseDto = new UserResponseDto(3L, "Peter", "Pan",
                "accountant@acme.com", List.of("ROLE_USER"));
        String expectedJson = objectMapper.writeValueAsString(responseDto);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminDtoJson)
        ).andExpect(status().isOk()
        ).andExpect(content().json(expectedJson));
    }

}
