package com.example.greetings.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Tag("integration")
@SpringBootTest
class GreetingsApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Nested
    class CreateGreeting {

        @Test
        void returns200WithGreeting() throws Exception {
            mvc.perform(post("/greetings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Ada\",\"language\":\"en\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.greeting").value("Hello, Ada!"))
                    .andExpect(jsonPath("$.language").value("en"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"EN", " en ", "en-US", "en_GB"})
        void normalizesLanguageAndReturnsCanonicalCode(String language) throws Exception {
            mvc.perform(post("/greetings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Ada\",\"language\":\"" + language + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.greeting").value("Hello, Ada!"))
                    .andExpect(jsonPath("$.language").value("en"));
        }

        @Test
        void unsupportedLanguageReturns400() throws Exception {
            mvc.perform(post("/greetings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Ada\",\"language\":\"xx\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").value("Unsupported language: 'xx'"));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.example.greetings.integration.GreetingsApiIntegrationTest#invalidBodies")
        void invalidBodyReturns400(String label, String body) throws Exception {
            mvc.perform(post("/greetings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        void malformedJsonReturns400() throws Exception {
            mvc.perform(post("/greetings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{not json"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetTypicalGreetings {

        @Test
        void returns200WithGreetings() throws Exception {
            mvc.perform(get("/greetings").param("lang", "es"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.language").value("es"))
                    .andExpect(jsonPath("$.greetings[0]").value("Hola"))
                    .andExpect(jsonPath("$.greetings.length()").value(3));
        }

        @Test
        void languageIsNormalized() throws Exception {
            mvc.perform(get("/greetings").param("lang", "EN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.language").value("en"));
        }

        @Test
        void missingLangReturns400() throws Exception {
            mvc.perform(get("/greetings"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @ParameterizedTest
        @ValueSource(strings = {"xx", ""})
        void unsupportedOrEmptyLangReturns400(String lang) throws Exception {
            mvc.perform(get("/greetings").param("lang", lang))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class OpenApiContract {

        @Test
        void operationIdsMatchTheSpec() throws Exception {
            mvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paths['/greetings'].post.operationId").value("createGreeting"))
                    .andExpect(jsonPath("$.paths['/greetings'].get.operationId").value("getTypicalGreetings"));
        }
    }

    static Stream<Arguments> invalidBodies() {
        return Stream.of(
                Arguments.of("empty object", "{}"),
                Arguments.of("missing language", "{\"name\":\"Ada\"}"),
                Arguments.of("missing name", "{\"language\":\"en\"}"),
                Arguments.of("empty name", "{\"name\":\"\",\"language\":\"en\"}"),
                Arguments.of("blank name", "{\"name\":\"   \",\"language\":\"en\"}"),
                Arguments.of("empty language", "{\"name\":\"Ada\",\"language\":\"\"}"),
                Arguments.of("null name", "{\"name\":null,\"language\":\"en\"}"));
    }
}
