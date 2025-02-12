package dev.maslycht.foreignsplitter.session;

import dev.maslycht.foreignsplitter.model.Item;
import dev.maslycht.foreignsplitter.model.Participant;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@SessionScope
@Getter
public class ForeignSplitterSession {
    private final List<Item> items = new ArrayList<>();
    private final List<Participant> participants = new ArrayList<>(List.of(
            new Participant("Alice"),
            new Participant("Bob")
    ));
    private BigDecimal foreignTotal = BigDecimal.ZERO;
    private BigDecimal localTotal = null;
    private BigDecimal exchangeRate = BigDecimal.ONE;

    public void addItem(Item item) {
        foreignTotal = foreignTotal.add(item.getForeignCost());
        recalculateExchangeRate();
        recalculateLocalCosts();
        recalculateParticipantLocalTotals();

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
        recalculateParticipantLocalTotals();

        items.remove(item);
    }

    public void setLocalTotal(BigDecimal localTotal) {
        this.localTotal = localTotal;
        recalculateExchangeRate();
        recalculateLocalCosts();
        recalculateParticipantLocalTotals();
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

    public void setParticipantItems(String participantId, List<String> itemIds) {
        Participant participant = participants.stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Participant not found: " + participantId));

        Set<Item> selectedItems = items.stream()
                .filter(item -> itemIds.contains(item.getId()))
                .collect(Collectors.toSet());

        if (selectedItems.equals(participant.getItems())) {
            return;
        }

        Set<Item> itemsToAdd = selectedItems.stream()
                .filter(item -> !participant.getItems().contains(item))
                .collect(Collectors.toSet());

        Set<Item> itemsToRemove = participant.getItems().stream()
                .filter(item -> !selectedItems.contains(item))
                .collect(Collectors.toSet());

        itemsToAdd.forEach(participant::addItem);
        itemsToRemove.forEach(participant::removeItem);

        recalculateParticipantLocalTotals();
    }

    private void recalculateParticipantLocalTotals() {
        participants.forEach(Participant::recalculateLocalTotal);
    }

    public boolean allItemsAreAssigned() {
        return items.stream().noneMatch(item -> item.getParticipants().isEmpty());
    }
}
