package com.inventory.dashboard.controller;

import com.inventory.dashboard.service.InventoryApiService;
import com.inventory.dashboard.model.Item;
import com.inventory.dashboard.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    
    private final InventoryApiService apiService;
    
    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Model model) {
        List<Item> items = apiService.getAllItems();
        List<Transaction> transactions = apiService.getAllTransactions();
        LocalDate today = LocalDate.now();

        long totalProducts = items.size();
        long lowStockCount = items.stream()
            .filter(i -> i.getQuantity() != null && i.getQuantity() > 0
                && i.getQuantity() <= (i.getMinQuantity() != null && i.getMinQuantity() > 0 ? i.getMinQuantity() : 5))
            .count();
        long outOfStockCount = items.stream()
            .filter(i -> i.getQuantity() != null && i.getQuantity() == 0)
            .count();
        long todayTxCount = transactions.stream()
            .filter(tx -> tx.getTimestamp() != null && tx.getTimestamp().toLocalDate().equals(today))
            .count();
        long totalTxCount = transactions.size();

        List<Transaction> recentTx = transactions.size() > 5
            ? transactions.subList(0, 5) : transactions;

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("todayTxCount", todayTxCount);
        model.addAttribute("totalTxCount", totalTxCount);
        model.addAttribute("recentTransactions", recentTx);
        return "home";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/inventory")
    public String inventory(Model model) {
        model.addAttribute("items", apiService.getAllItems());
        return "inventory";
    }
    
    @GetMapping("/transactions")
    public String transactions(Model model) {
        List<Transaction> transactions = apiService.getAllTransactions();
        long incomingCount = transactions.stream()
            .filter(tx -> "IN".equals(tx.getType()))
            .count();
        long outgoingCount = transactions.stream()
            .filter(tx -> "OUT".equals(tx.getType()))
            .count();
        long adjustCount = transactions.stream()
            .filter(tx -> "ADJUST".equals(tx.getType()))
            .count();

        
        LocalDate today = LocalDate.now();
        DateTimeFormatter chartFmt = DateTimeFormatter.ofPattern("dd MMM");
        List<String> chartLabels = new ArrayList<>();
        List<Long> chartIn = new ArrayList<>();
        List<Long> chartOut = new ArrayList<>();
        List<Long> chartAdj = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            chartLabels.add(d.format(chartFmt));
            chartIn.add(transactions.stream().filter(tx -> tx.getTimestamp() != null && tx.getTimestamp().toLocalDate().equals(d) && "IN".equals(tx.getType())).count());
            chartOut.add(transactions.stream().filter(tx -> tx.getTimestamp() != null && tx.getTimestamp().toLocalDate().equals(d) && "OUT".equals(tx.getType())).count());
            chartAdj.add(transactions.stream().filter(tx -> tx.getTimestamp() != null && tx.getTimestamp().toLocalDate().equals(d) && "ADJUST".equals(tx.getType())).count());
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("transactionCount", transactions.size());
        model.addAttribute("incomingCount", incomingCount);
        model.addAttribute("outgoingCount", outgoingCount);
        model.addAttribute("adjustCount", adjustCount);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartIn", chartIn);
        model.addAttribute("chartOut", chartOut);
        model.addAttribute("chartAdj", chartAdj);
        return "transactions";
    }
    
    @GetMapping("/items")
    public String itemsManagement(Model model) {
        model.addAttribute("items", apiService.getAllItems());
        return "items";
    }
    
    @GetMapping("/users")
    public String usersManagement() {
        return "users";
    }
    
    @GetMapping("/categories")
    public String categoriesManagement() {
        return "categories";
    }
    
    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }
    
    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    @GetMapping("/database")
    public String database() {
        return "database";
    }
    
    @PostMapping("/items/add")
    public String addItem(
            @RequestParam String name,
            @RequestParam String barcode,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            RedirectAttributes redirectAttributes
    ) {
        try {
            apiService.createItem(name, barcode, quantity, location, category);
            redirectAttributes.addFlashAttribute("message", "Produs adăugat cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la adăugarea produsului: " + e.getMessage());
        }
        return "redirect:/items";
    }
    
    @PostMapping("/items/update")
    public String updateItem(
            @RequestParam Long id,
            @RequestParam String name,
            @RequestParam String barcode,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            RedirectAttributes redirectAttributes
    ) {
        try {
            apiService.updateItem(id, name, barcode, quantity, location, category);
            redirectAttributes.addFlashAttribute("message", "Produs actualizat cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la actualizarea produsului: " + e.getMessage());
        }
        return "redirect:/items";
    }
    
    @PostMapping("/items/delete/{id}")
    public String deleteItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            apiService.deleteItem(id);
            redirectAttributes.addFlashAttribute("message", "Produs șters cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la ștergerea produsului: " + e.getMessage());
        }
        return "redirect:/items";
    }
    
    @PostMapping("/transactions/delete/{id}")
    public String deleteTransaction(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            apiService.deleteTransaction(id);
            redirectAttributes.addFlashAttribute("message", "Tranzacție ștearsă cu succes!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Eroare la ștergerea tranzacției: " + e.getMessage());
        }
        return "redirect:/transactions";
    }
}
