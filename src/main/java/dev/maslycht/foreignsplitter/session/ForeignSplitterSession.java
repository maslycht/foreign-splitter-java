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
    private final List<Participant> participants = new ArrayList<>();
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

    public void addParticipant(String name) {
        participants.add(new Participant(name));
    }

    public void removeItem(String itemId) {
        Item item = items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        foreignTotal = foreignTotal.subtract(item.getForeignCost());
        item.getParticipants().forEach(participant -> participant.removeItem(item));

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
        if (foreignTotal.compareTo(BigDecimal.ZERO) == 0 || localTotal.compareTo(BigDecimal.ZERO) == 0) {
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
        Participant participant = getParticipantById(participantId);

        Set<Item> selectedItems = items.stream()
                .filter(item -> itemIds.contains(item.getId()))
                .collect(Collectors.toSet());

        if (selectedItems.equals(participant.getItems())) {
            return;
        }

        Set<Item> currentItems = Set.copyOf(participant.getItems());

        selectedItems.stream()
                .filter(selectedItem -> !currentItems.contains(selectedItem))
                .forEach(participant::addItem);

        currentItems.stream()
                .filter(selectedItem -> !selectedItems.contains(selectedItem))
                .forEach(participant::removeItem);

        recalculateParticipantLocalTotals();
    }

    private Participant getParticipantById(String participantId) {
        return participants.stream()
                .filter(p -> p.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Participant not found: " + participantId));
    }

    private void recalculateParticipantLocalTotals() {
        participants.forEach(Participant::recalculateLocalTotal);
    }

    public boolean allItemsAreAssigned() {
        return items.stream().noneMatch(item -> item.getParticipants().isEmpty());
    }

    public void removeParticipant(String participantId) {
        Participant participant = getParticipantById(participantId);
        participant.removeAllItems();
        participants.remove(participant);
        recalculateParticipantLocalTotals();
    }

    public void reset() {
        items.clear();
        participants.clear();
        foreignTotal = BigDecimal.ZERO;
        localTotal = null;
        exchangeRate = BigDecimal.ONE;
    }
}
