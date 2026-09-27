package com.akarine.catalogue.assistant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssistantController.class)
class AssistantControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AssistantService assistantService;

    @Test
    void renvoieLaReponseDeLAssistant() throws Exception {
        when(assistantService.ask("Une perceuse à moins de 100 € ?"))
                .thenReturn("Oui : la perceuse à percussion Bosch à 79,90 €, en stock.");

        mvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"Une perceuse à moins de 100 € ?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Oui : la perceuse à percussion Bosch à 79,90 €, en stock."));
    }

    @Test
    void questionVideRefusee() throws Exception {
        mvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"  "}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(assistantService);
    }

    @Test
    void sansModeleConfigureRenvoie503() throws Exception {
        when(assistantService.ask("Bonjour")).thenThrow(new AssistantUnavailableException());

        mvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"Bonjour"}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Assistant indisponible"));
    }
}
