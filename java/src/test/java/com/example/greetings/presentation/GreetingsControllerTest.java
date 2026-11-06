package com.example.greetings.presentation;

import static com.example.greetings.support.TestProfiles.ENGLISH;
import static com.example.greetings.support.TestProfiles.SPANISH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.greetings.application.CreateGreeting;
import com.example.greetings.application.GetTypicalGreetings;
import com.example.greetings.support.FakeLanguageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class GreetingsControllerTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var repository = new FakeLanguageRepository(ENGLISH, SPANISH);
        var controller = new GreetingsController(
                new CreateGreeting(repository), new GetTypicalGreetings(repository));
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void postReturnsGreeting() throws Exception {
        mvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada\",\"language\":\"en\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.greeting").value("Hello, Ada!"))
                .andExpect(jsonPath("$.language").value("en"));
    }

    @Test
    void postWithUnsupportedLanguageReturns400WithProblemDetail() throws Exception {
        mvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada\",\"language\":\"xx\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Unsupported language: 'xx'"));
    }

    @Test
    void postWithBlankNameReturns400() throws Exception {
        mvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \",\"language\":\"en\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Name must not be blank"));
    }

    @Test
    void postWithMissingFieldsReturns400() throws Exception {
        mvc.perform(post("/greetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReturnsTypicalGreetings() throws Exception {
        mvc.perform(get("/greetings").param("lang", "es"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("es"))
                .andExpect(jsonPath("$.greetings[0]").value("Hola"))
                .andExpect(jsonPath("$.greetings.length()").value(2));
    }

    @Test
    void getWithUnsupportedLanguageReturns400() throws Exception {
        mvc.perform(get("/greetings").param("lang", "xx"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWithoutLangReturns400() throws Exception {
        mvc.perform(get("/greetings"))
                .andExpect(status().isBadRequest());
    }
}