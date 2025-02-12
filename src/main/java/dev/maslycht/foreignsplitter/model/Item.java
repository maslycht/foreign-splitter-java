package dev.maslycht.foreignsplitter.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Item {
    private final String id;
    private String name;
    private BigDecimal foreignCost;
    private BigDecimal localCost;

    public Item() {
        this.id = UUID.randomUUID().toString();
    }
}
