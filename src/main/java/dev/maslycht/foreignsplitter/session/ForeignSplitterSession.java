package dev.maslycht.foreignsplitter.session;

import dev.maslycht.foreignsplitter.model.Item;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
@SessionScope
@Getter
public class ForeignSplitterSession {
    private final List<Item> items = new ArrayList<>();
    private BigDecimal foreignTotal = BigDecimal.ZERO;
    private BigDecimal localTotal = null;
    private BigDecimal exchangeRate = BigDecimal.ONE;

    public void addItem(Item item) {
        foreignTotal = foreignTotal.add(item.getForeignCost());
        recalculateExchangeRate();
        recalculateLocalCosts();

        BigDecimal localCost = item.getForeignCost().multiply(exchangeRate);
        item.setLocalCost(localCost);
        items.add(item);
    }

    public void removeItem(String itemId) {
        Item item = items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        foreignTotal = foreignTotal.subtract(item.getForeignCost());
        recalculateExchangeRate();
        recalculateLocalCosts();

        items.remove(item);
    }

    public void setLocalTotal(BigDecimal localTotal) {
        this.localTotal = localTotal;
        recalculateExchangeRate();
        recalculateLocalCosts();
    }

    private void recalculateExchangeRate() {
        if (foreignTotal.equals(BigDecimal.ZERO) || localTotal.equals(BigDecimal.ZERO)) {
            exchangeRate = BigDecimal.ONE;
            return;
        }
        exchangeRate = localTotal.divide(foreignTotal, 10, RoundingMode.HALF_UP);
    }

    private void recalculateLocalCosts() {
        items.forEach(item -> item.setLocalCost(
                item.getForeignCost().multiply(exchangeRate)
        ));
    }

}
