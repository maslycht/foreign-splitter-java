package dev.maslycht.foreignsplitter.controller;

import dev.maslycht.foreignsplitter.model.Item;
import dev.maslycht.foreignsplitter.session.ForeignSplitterSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@AllArgsConstructor
public class ForeignSplitterController {
    private final ForeignSplitterSession session;

    @ModelAttribute("items")
    public List<Item> items() {
        return session.getItems();
    }

    @ModelAttribute("exchangeRate")
    public BigDecimal exchangeRate() {
        return session.getExchangeRate();
    }

    @ModelAttribute("foreignTotal")
    public BigDecimal foreignTotal() {
        return session.getForeignTotal();
    }

    @ModelAttribute("localTotal")
    public BigDecimal localTotal() {
        return session.getLocalTotal();
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
}
