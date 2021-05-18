package com.lrhealth.bcos.blockchainlogger.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class BcosControllerTest {
    @Autowired
    private MockMvc mvc;

    private static String savedLogid;

    @Test
    @Order(1) 
    public void testAddlog() throws JsonProcessingException, Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        savedLogid = mvc
                .perform(MockMvcRequestBuilders.post("/addlog").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LogRequestBean("yinwenbao", "/api/test/add", "test content..."))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        System.out.println(savedLogid);
    }

    @Test
    @Order(2) 
    public void testVerifylog() throws JsonProcessingException, Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        mvc.perform(MockMvcRequestBuilders.post("/verifylog").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new VerifyRequestBean(savedLogid, "test content..."))))
                .andExpect(status().isOk()).andExpect(content().string(equalTo("true")));
    }
}
