package com.example.walletapp.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deposit_ShouldIncreaseBalance() throws Exception {
        String requestJson = """
            {
                "walletId": "11111111-1111-1111-1111-111111111111",
                "operationType": "DEPOSIT",
                "amount": 1000
            }""";

        mockMvc.perform(post("/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void invalidOperationType_ShouldReturnBadRequest() throws Exception {
        String invalidRequest = """
            {
                "walletId": "11111111-1111-1111-1111-111111111111",
                "operationType": "DEPOSIT1",
                "amount": 100
            }""";

        mockMvc.perform(post("/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_OPERATION_TYPE"));
    }
}