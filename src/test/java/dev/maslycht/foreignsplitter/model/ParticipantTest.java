package dev.maslycht.foreignsplitter.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

public class ParticipantTest {

    @Test
    public void testAddItem() {
        Participant participant = new Participant("Alice");
        Item item = new Item();
        item.setLocalCost(new BigDecimal("100.00"));

        // initially, localTotal should be 0
        assertEquals(0, BigDecimal.ZERO.compareTo(participant.getLocalTotal()));

        participant.addItem(item);

        // for a single participant, item.getSplitLocalCost returns localCost
        BigDecimal expected = new BigDecimal("100.00").setScale(10, RoundingMode.HALF_UP);
        assertEquals(0, expected.compareTo(participant.getLocalTotal().setScale(10, RoundingMode.HALF_UP)));
        
        // verify the item is in the participant's items and vice versa
        assertTrue(participant.getItems().contains(item));
        assertTrue(item.getParticipants().contains(participant));
    }

    @Test
    public void testRemoveItem() {
        Participant participant = new Participant("Bob");
        Item item = new Item();
        item.setLocalCost(new BigDecimal("100.00"));

        participant.addItem(item);
        participant.removeItem(item);
        
        // after removal, localTotal should be 0 and the associations removed
        assertEquals(0, BigDecimal.ZERO.compareTo(participant.getLocalTotal()));
        assertFalse(participant.getItems().contains(item));
        assertFalse(item.getParticipants().contains(participant));
    }

    @Test
    public void testRemoveAllItems() {
        Participant participant = new Participant("Charlie");
        Item item1 = new Item();
        item1.setLocalCost(new BigDecimal("100.00"));
        Item item2 = new Item();
        item2.setLocalCost(new BigDecimal("200.00"));

        participant.addItem(item1);
        participant.addItem(item2);

        participant.removeAllItems();

        // verify that all items are removed and localTotal is reset
        assertTrue(participant.getItems().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(participant.getLocalTotal()));
        
        // verify that participant is removed from items' participant lists
        assertFalse(item1.getParticipants().contains(participant));
        assertFalse(item2.getParticipants().contains(participant));
    }

    @Test
    public void testRecalculateLocalTotal() {
        Participant participant = new Participant("Diana");
        Item item1 = new Item();
        item1.setLocalCost(new BigDecimal("100.00"));
        Item item2 = new Item();
        item2.setLocalCost(new BigDecimal("200.00"));

        participant.addItem(item1);
        participant.addItem(item2);

        // with a single participant for each item, split cost equals the full local cost
        BigDecimal expected = new BigDecimal("300.00").setScale(10, RoundingMode.HALF_UP);
        assertEquals(0, expected.compareTo(participant.getLocalTotal().setScale(10, RoundingMode.HALF_UP)));

        // simulate another participant sharing item1
        Participant another = new Participant("Eve");
        item1.getParticipants().add(another);

        // recalculate participant's local total
        participant.recalculateLocalTotal();

        // for item1, split cost becomes 100.00 / 2 = 50.00; item2 remains 200.00; total = 250.00
        BigDecimal expectedNew = new BigDecimal("250.00").setScale(10, RoundingMode.HALF_UP);
        assertEquals(0, expectedNew.compareTo(participant.getLocalTotal().setScale(10, RoundingMode.HALF_UP)));
    }
} 