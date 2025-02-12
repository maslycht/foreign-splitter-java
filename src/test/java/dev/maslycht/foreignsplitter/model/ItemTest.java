package dev.maslycht.foreignsplitter.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    @Test
    public void testGetSplitLocalCostWithoutParticipants() {
        Item item = new Item();
        item.setLocalCost(new BigDecimal("100.00"));

        // when no participants, getSplitLocalCost should return the localCost directly
        BigDecimal splitCost = item.getSplitLocalCost();
        assertEquals(0, new BigDecimal("100.00").compareTo(splitCost));
    }

    @Test
    public void testGetSplitLocalCostWithParticipants() {
        Item item = new Item();
        item.setLocalCost(new BigDecimal("100.00"));
        
        // create two dummy participants
        Participant participant1 = new Participant("Alice");
        Participant participant2 = new Participant("Bob");

        // manually add participants to the item's participant set
        item.getParticipants().add(participant1);
        item.getParticipants().add(participant2);

        // expected split: 100.00 / 2 = 50.00, scaled to 10 decimal places (for precision)
        BigDecimal expected = new BigDecimal("50.00").setScale(10, RoundingMode.HALF_UP);
        BigDecimal actual = item.getSplitLocalCost();
        assertEquals(0, expected.compareTo(actual));
    }
} 