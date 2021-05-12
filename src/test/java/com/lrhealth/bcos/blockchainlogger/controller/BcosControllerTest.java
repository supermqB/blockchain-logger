package com.lrhealth.bcos.blockchainlogger.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class BcosControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void getHello() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().string(equalTo("yes, I'm done!")));
    }

    @Test
    public void testAddlog() throws JsonProcessingException, Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String logid = mvc
                .perform(MockMvcRequestBuilders.post("/addlog").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LogRequestBean("yinwenbao", "/api/test/add", "test content..."))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        System.out.println(logid);
    }

    @Test
    public void testVerifylog() throws JsonProcessingException, Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        mvc.perform(MockMvcRequestBuilders.post("/verifylog").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new VerifyRequestBean("979e2087df4b80a5dca377766cc8465f", "test content..."))))
                .andExpect(status().isOk()).andExpect(content().string(equalTo("true")));
    }
}
