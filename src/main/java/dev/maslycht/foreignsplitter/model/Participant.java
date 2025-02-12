package dev.maslycht.foreignsplitter.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Participant {
    private final String id = UUID.randomUUID().toString();
    private String name;
    private final Set<Item> items = new HashSet<>();
    private BigDecimal localTotal = BigDecimal.ZERO;

    public Participant(String name) {
        this.name = name;
    }

    public void addItem(Item item) {
        items.add(item);
        item.getParticipants().add(this);
        localTotal = localTotal.add(item.getSplitLocalCost());
    }

    public void removeItem(Item item) {
        items.remove(item);
        localTotal = localTotal.subtract(item.getSplitLocalCost());
        item.getParticipants().remove(this);
    }

    public void removeAllItems() {
        Set.copyOf(items).forEach(this::removeItem);
    }

    public void recalculateLocalTotal() {
        localTotal = items.stream()
                .map(Item::getSplitLocalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
