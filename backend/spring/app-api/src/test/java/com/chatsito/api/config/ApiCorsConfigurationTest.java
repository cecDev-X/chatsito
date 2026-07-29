package com.chatsito.api.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

class ApiCorsConfigurationTest {
    @Test
    void acceptsTheFrontendOriginAuthorizationHeaderAndAnyHttpMethod() throws Exception {
        try (var context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.register(TestConfiguration.class);
            context.refresh();
            var mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

            mockMvc.perform(options("/probe")
                            .header("Origin", "http://localhost:3000")
                            .header("Access-Control-Request-Method", "PATCH")
                            .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                    .andExpect(header().string("Access-Control-Allow-Methods", containsString("PATCH")))
                    .andExpect(header().string("Access-Control-Allow-Headers", containsString("Authorization")));
        }
    }

    @Configuration
    @EnableWebMvc
    @Import(ApiCorsConfiguration.class)
    static class TestConfiguration {
        @Bean
        ProbeController probeController() {
            return new ProbeController();
        }
    }

    @Controller
    static class ProbeController {
        @GetMapping("/probe")
        void probe() {
        }
    }
}
