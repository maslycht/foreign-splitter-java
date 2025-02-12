package dev.maslycht.foreignsplitter.session;

import dev.maslycht.foreignsplitter.model.Item;
import dev.maslycht.foreignsplitter.model.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ForeignSplitterSessionTest {

    private ForeignSplitterSession session;

    @BeforeEach
    public void setUp() {
        session = new ForeignSplitterSession();
    }

    @Test
    public void testAddItem() {
        // set the local total so that exchange rate can be calculated
        session.setLocalTotal(new BigDecimal("100.00"));

        Item item = new Item();
        item.setForeignCost(new BigDecimal("10.00"));

        session.addItem(item);

        // foreignTotal should now be 10.00
        assertEquals(0, new BigDecimal("10.00").compareTo(session.getForeignTotal()));

        // exchangeRate = localTotal / foreignTotal = 100 / 10 = 10.00 (with proper scale)
        BigDecimal expectedExchangeRate = new BigDecimal("10.00");
        assertEquals(0, expectedExchangeRate.compareTo(session.getExchangeRate().setScale(2, RoundingMode.HALF_UP)));

        // local cost of item should be foreignCost * exchangeRate = 10 * 10 = 100.00
        assertEquals(0, new BigDecimal("100.00").compareTo(item.getLocalCost().setScale(2, RoundingMode.HALF_UP)));

        // check that the item is added to the session
        assertTrue(session.getItems().contains(item));
    }

    @Test
    public void testRemoveItem() {
        // set the local total so that exchange rate can be calculated
        session.setLocalTotal(new BigDecimal("100.00"));

        Item itemToKeep = new Item();
        itemToKeep.setForeignCost(new BigDecimal("10.00"));

        Item itemToRemove = new Item();
        itemToRemove.setForeignCost(new BigDecimal("20.00"));

        session.addItem(itemToKeep);
        session.addItem(itemToRemove);

        // Remove the item
        session.removeItem(itemToRemove.getId());

        // The item should be removed and foreignTotal adjusted to the cost of item to keep
        assertFalse(session.getItems().contains(itemToRemove));
        assertEquals(0, new BigDecimal("10.00").compareTo(session.getForeignTotal()));
    }

    @Test
    public void testSetLocalTotal() {
        // set the local total so that exchange rate can be calculated
        session.setLocalTotal(new BigDecimal("100.00"));

        Item item = new Item();
        item.setForeignCost(new BigDecimal("10.00"));

        session.addItem(item);

        // update local total and recalculate
        session.setLocalTotal(new BigDecimal("200.00"));

        // now exchangeRate = 200 / 10 = 20.00
        BigDecimal expectedExchangeRate = new BigDecimal("20.00");
        assertEquals(0, expectedExchangeRate.compareTo(session.getExchangeRate().setScale(2, RoundingMode.HALF_UP)));

        // item's local cost should be 10 * 20 = 200.00
        assertEquals(0, new BigDecimal("200.00").compareTo(item.getLocalCost().setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    public void testSetParticipantItems() {
        // set the local total so that exchange rate can be calculated
        session.setLocalTotal(new BigDecimal("100.00"));

        // add two items
        Item item1 = new Item();
        item1.setForeignCost(new BigDecimal("10.00"));

        Item item2 = new Item();
        item2.setForeignCost(new BigDecimal("20.00"));

        session.addItem(item1);
        session.addItem(item2);

        // add a participant
        session.addParticipant("Bob");
        Participant participant = session.getParticipants().getFirst();

        // initially, participant should have no items
        assertTrue(participant.getItems().isEmpty());

        // assign both items to the participant
        session.setParticipantItems(participant.getId(), List.of(item1.getId(), item2.getId()));
        assertEquals(2, participant.getItems().size());

        // check that each item has the participant assigned
        for (Item item : participant.getItems()) {
            assertTrue(item.getParticipants().contains(participant));
        }

        // remove item2
        session.setParticipantItems(participant.getId(), List.of(item1.getId()));

        // ensure the participant only has item1 left
        assertEquals(1, participant.getItems().size());
        assertTrue(participant.getItems().stream().anyMatch(i -> i.getId().equals(item1.getId())));

        // ensure the participant's total is equal to item1 local cost: (100 / 30) * 10 = 33.33
        assertEquals(0, new BigDecimal("33.33").compareTo(participant.getLocalTotal().setScale(2, RoundingMode.HALF_UP)));

        // ensure item2 no longer has the participant
        assertFalse(item2.getParticipants().contains(participant));
    }

    @Test
    public void testAllItemsAssigned() {
        // set the local total so that exchange rate can be calculated
        session.setLocalTotal(new BigDecimal("100.00"));

        // add an item with no participants
        Item item = new Item();
        item.setForeignCost(new BigDecimal("10.00"));
        session.addItem(item);

        // initially, not all items are assigned
        assertFalse(session.allItemsAreAssigned());

        // add a participant and assign the item
        session.addParticipant("Charlie");
        Participant participant = session.getParticipants().getFirst();
        session.setParticipantItems(participant.getId(), List.of(item.getId()));

        // ensure the flag is updated
        assertTrue(session.allItemsAreAssigned());
    }

    @Test
    public void testRemoveParticipant() {
        // set the local total so that exchange rate can be calculated
        session.setLocalTotal(new BigDecimal("100.00"));

        // add an item
        Item item = new Item();
        item.setForeignCost(new BigDecimal("10.00"));

        session.addItem(item);

        // add a participant and assign the item
        session.addParticipant("Dave");
        Participant participant = session.getParticipants().getFirst();
        session.setParticipantItems(participant.getId(), List.of(item.getId()));

        // remove the participant
        session.removeParticipant(participant.getId());

        // ensure the session no longer contains the participant
        assertTrue(session.getParticipants().isEmpty());

        // ensure the item no longer references the removed participant
        assertFalse(item.getParticipants().contains(participant));
    }

    @Test
    public void testReset() {
        // set the local total so that exchange rate can be calculated
        session.setLocalTotal(new BigDecimal("100.00"));

        // set up additional session data
        Item item = new Item();
        item.setForeignCost(new BigDecimal("10.00"));
        session.addItem(item);
        session.addParticipant("Eve");

        // check the session is not empty
        assertFalse(session.getItems().isEmpty());
        assertFalse(session.getParticipants().isEmpty());

        session.reset();

        // ensure the session was reset to the default values
        assertTrue(session.getItems().isEmpty());
        assertTrue(session.getParticipants().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(session.getForeignTotal()));
        assertNull(session.getLocalTotal());
        assertEquals(0, BigDecimal.ONE.compareTo(session.getExchangeRate()));
    }
} 