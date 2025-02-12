package dev.maslycht.foreignsplitter.controller;

import dev.maslycht.foreignsplitter.model.Item;
import dev.maslycht.foreignsplitter.model.Participant;
import dev.maslycht.foreignsplitter.session.ForeignSplitterSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Controller
@AllArgsConstructor
public class ForeignSplitterController {
    private final ForeignSplitterSession session;

    @ModelAttribute("items")
    public List<Item> items() {
        return session.getItems();
    }

    @ModelAttribute("participants")
    public List<Participant> participants() {
        return session.getParticipants();
    }

    @ModelAttribute("exchangeRate")
    public BigDecimal exchangeRate() {
        return session.getExchangeRate().setScale(2, RoundingMode.HALF_UP);
    }

    @ModelAttribute("foreignTotal")
    public BigDecimal foreignTotal() {
        return session.getForeignTotal().setScale(2, RoundingMode.HALF_UP);
    }

    @ModelAttribute("localTotal")
    public BigDecimal localTotal() {
        BigDecimal localTotal = session.getLocalTotal();
        if (localTotal == null) return null;
        return localTotal.setScale(2, RoundingMode.HALF_UP);
    }

    @ModelAttribute("allItemsAssigned")
    public boolean allItemsAssigned() {
        return session.allItemsAreAssigned();
    }

    @GetMapping
    public String home(Model model) {
        model.addAttribute("newItem", new Item());
        return "home";
    }

    @PostMapping("/saveItem")
    public String saveItem(@ModelAttribute("newItem") Item item) {
        session.addItem(item);
        return "redirect:/";
    }

    @PostMapping("/deleteItem")
    public String deleteItem(@RequestParam("itemId") String itemId) {
        session.removeItem(itemId);
        return "redirect:/";
    }

    @PostMapping("/setLocalTotal")
    public String setLocalTotal(@RequestParam("localTotal") float localTotal) {
        session.setLocalTotal(BigDecimal.valueOf(localTotal));
        return "redirect:/";
    }

    @PostMapping("/setParticipantItems")
    public String setParticipantItems(
            @RequestParam("participantId") String participantId,
            @RequestParam(value = "itemIds", required = false) List<String> itemIds
    ) {
        if (itemIds == null) itemIds = List.of();

        session.setParticipantItems(participantId, itemIds);
        return "redirect:/";
    }

    @PostMapping("/addParticipant")
    public String addParticipant(@RequestParam("name") String name) {
        session.addParticipant(name);
        return "redirect:/";
    }

    @PostMapping("/removeParticipant")
    public String removeParticipant(@RequestParam("participantId") String participantId) {
        session.removeParticipant(participantId);
        return "redirect:/";
    }
}
