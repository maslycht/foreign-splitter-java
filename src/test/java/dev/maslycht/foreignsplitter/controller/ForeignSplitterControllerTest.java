package dev.maslycht.foreignsplitter.controller;

import dev.maslycht.foreignsplitter.model.Item;
import dev.maslycht.foreignsplitter.session.ForeignSplitterSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ForeignSplitterController.class)
public class ForeignSplitterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ForeignSplitterSession session;

    @BeforeEach
    public void setup() {
        when(session.getExchangeRate()).thenReturn(new BigDecimal("1.0"));
        when(session.getItems()).thenReturn(new ArrayList<>());
        when(session.getParticipants()).thenReturn(new ArrayList<>());
        when(session.getLocalTotal()).thenReturn(BigDecimal.ZERO);
        when(session.getForeignTotal()).thenReturn(BigDecimal.ZERO);
    }

    @Test
    public void testHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("newItem"))
                .andExpect(view().name("home"));
    }

    @Test
    public void testSaveItem() throws Exception {
        mockMvc.perform(post("/saveItem")
                                .param("name", "Test Item")
                                .param("foreignCost", "10.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // verify that session.addItem was called with an Item instance
        verify(session).addItem(any(Item.class));
    }

    @Test
    public void testDeleteItem() throws Exception {
        String itemId = "item123";
        mockMvc.perform(post("/deleteItem")
                                .param("itemId", itemId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // verify that session.removeItem was called with the item ID
        verify(session).removeItem(itemId);
    }

    @Test
    public void testSetLocalTotal() throws Exception {
        mockMvc.perform(post("/setLocalTotal")
                                .param("localTotal", "100.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // verify that session.setLocalTotal was called with a value of 100.00
        verify(session).setLocalTotal(new BigDecimal("100.00"));
    }

    @Test
    public void testSetParticipantItems() throws Exception {
        String participantId = "participant1";
        String[] itemIds = new String[] {"item1", "item2"};
        mockMvc.perform(post("/setParticipantItems")
                                .param("participantId", participantId)
                                .param("itemIds", itemIds))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // verify that session.setParticipantItems was called with the correct params
        verify(session).setParticipantItems(participantId, List.of("item1", "item2"));
    }

    @Test
    public void testAddParticipant() throws Exception {
        String name = "Alice";
        mockMvc.perform(post("/addParticipant")
                                .param("name", name))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // verify that session.addParticipant was called with the correct name
        verify(session).addParticipant(name);
    }

    @Test
    public void testRemoveParticipant() throws Exception {
        String participantId = "participant1";
        mockMvc.perform(post("/removeParticipant")
                                .param("participantId", participantId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // verify that session.removeParticipant was called with the correct ID
        verify(session).removeParticipant(participantId);
    }

    @Test
    public void testReset() throws Exception {
        mockMvc.perform(post("/reset"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // verify that the session.reset method was called
        verify(session).reset();
    }
} 