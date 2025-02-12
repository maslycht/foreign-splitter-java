package dev.maslycht.foreignsplitter.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Item {
    private final String id = UUID.randomUUID().toString();
    private final Set<Participant> participants = new HashSet<>();
    private String name;
    private BigDecimal foreignCost;
    private BigDecimal localCost;

    public BigDecimal getSplitLocalCost() {
        if (participants.isEmpty()) return localCost;
        return localCost.divide(BigDecimal.valueOf(participants.size()), 10, RoundingMode.HALF_UP);
    }
}
