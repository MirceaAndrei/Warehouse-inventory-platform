package com.inventory.system.bootstrap;

import com.inventory.system.domain.Category;
import com.inventory.system.domain.Item;
import com.inventory.system.domain.StockSettings;
import com.inventory.system.domain.Transaction;
import com.inventory.system.domain.User;
import com.inventory.system.domain.Role;
import com.inventory.system.repository.CategoryRepository;
import com.inventory.system.repository.ItemRepository;
import com.inventory.system.repository.StockSettingsRepository;
import com.inventory.system.repository.TransactionRepository;
import com.inventory.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    
    private final ItemRepository itemRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StockSettingsRepository stockSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        loadDemoUsers();
        loadDemoCategories();
        loadStockSettings();
        loadDemoData();
    }
    
    private void loadDemoUsers() {
        if (userRepository.count() == 0) {
            System.out.println("[AUTH] Loading demo users...");
            
            
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@inventory.com");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setMustChangePassword(false);
            userRepository.save(admin);
            
            
            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setEmail("manager@inventory.com");
            manager.setRole(Role.MANAGER);
            manager.setEnabled(true);
            manager.setMustChangePassword(false);
            userRepository.save(manager);
            
            
            User employee = new User();
            employee.setUsername("employee");
            employee.setPassword(passwordEncoder.encode("employee123"));
            employee.setEmail("employee@inventory.com");
            employee.setRole(Role.EMPLOYEE);
            employee.setEnabled(true);
            employee.setMustChangePassword(false);
            userRepository.save(employee);
            
            System.out.println("[OK] Demo users loaded!");
            System.out.println("   - admin / admin123 (ADMIN)");
            System.out.println("   - manager / manager123 (MANAGER)");
            System.out.println("   - employee / employee123 (EMPLOYEE)");
        }
    }
    
    private void loadDemoCategories() {
        if (categoryRepository.count() == 0) {
            System.out.println("📁 Loading demo categories...");
            
            Category electronics = new Category(null, "Electronics", "Electronic devices and gadgets", true);
            categoryRepository.save(electronics);
            
            Category accessories = new Category(null, "Accessories", "Computer and phone accessories", true);
            categoryRepository.save(accessories);
            
            Category office = new Category(null, "Office Supplies", "Office equipment and supplies", true);
            categoryRepository.save(office);
            
            Category furniture = new Category(null, "Furniture", "Office and home furniture", true);
            categoryRepository.save(furniture);
            
            Category tools = new Category(null, "Tools", "Hand tools and power tools", true);
            categoryRepository.save(tools);
            
            Category other = new Category(null, "Other", "Miscellaneous items", true);
            categoryRepository.save(other);
            
            System.out.println("[OK] Demo categories loaded! Count: " + categoryRepository.count());
        }
    }
    
    private void loadStockSettings() {
        if (stockSettingsRepository.count() == 0) {
            System.out.println("⚙️ Loading stock settings...");
            
            StockSettings lowStock = new StockSettings();
            lowStock.setSettingKey("LOW_STOCK_THRESHOLD");
            lowStock.setSettingValue(10);
            lowStock.setDescription("Alert when quantity is below or equal to this value");
            stockSettingsRepository.save(lowStock);
            
            StockSettings outOfStock = new StockSettings();
            outOfStock.setSettingKey("OUT_OF_STOCK_THRESHOLD");
            outOfStock.setSettingValue(0);
            outOfStock.setDescription("Alert when quantity is at or below this value");
            stockSettingsRepository.save(outOfStock);
            
            System.out.println("[OK] Stock settings loaded!");
            System.out.println("   - LOW_STOCK_THRESHOLD: 10");
            System.out.println("   - OUT_OF_STOCK_THRESHOLD: 0");
        }
    }
    
    private void loadDemoData() {
        if (itemRepository.count() == 0) {
            System.out.println("📦 Loading demo data...");
            
            
            Item laptop = new Item();
            laptop.setBarcode("1234567890");
            laptop.setName("Laptop Dell XPS 15");
            laptop.setDescription("High-performance laptop");
            laptop.setQuantity(10);
            laptop.setMinQuantity(3);
            laptop.setLocation("Shelf A1");
            laptop.setCategory("Electronics");
            itemRepository.save(laptop);
            
            Item mouse = new Item();
            mouse.setBarcode("9876543210");
            mouse.setName("Logitech MX Master 3");
            mouse.setDescription("Wireless mouse");
            mouse.setQuantity(25);
            mouse.setMinQuantity(5);
            mouse.setLocation("Shelf A2");
            mouse.setCategory("Accessories");
            itemRepository.save(mouse);
            
            Item keyboard = new Item();
            keyboard.setBarcode("1111222233");
            keyboard.setName("Mechanical Keyboard");
            keyboard.setDescription("RGB mechanical keyboard");
            keyboard.setQuantity(15);
            keyboard.setMinQuantity(4);
            keyboard.setLocation("Shelf A3");
            keyboard.setCategory("Accessories");
            itemRepository.save(keyboard);
            
            Item monitor = new Item();
            monitor.setBarcode("4444555566");
            monitor.setName("Monitor 27\" 4K");
            monitor.setDescription("4K UHD monitor");
            monitor.setQuantity(8);
            monitor.setMinQuantity(2);
            monitor.setLocation("Shelf B1");
            monitor.setCategory("Electronics");
            itemRepository.save(monitor);
            
            Item phone = new Item();
            phone.setBarcode("7777888899");
            phone.setName("Samsung Galaxy S23");
            phone.setDescription("Latest flagship phone");
            phone.setQuantity(20);
            phone.setMinQuantity(5);
            phone.setLocation("Shelf C1");
            phone.setCategory("Electronics");
            itemRepository.save(phone);
            
            
            Transaction t1 = new Transaction();
            t1.setItem(laptop);
            t1.setType(Transaction.TransactionType.IN);
            t1.setQuantity(5);
            t1.setNotes("Initial stock");
            t1.setDeviceId("DEMO");
            t1.setTimestamp(LocalDateTime.now().minusDays(5));
            transactionRepository.save(t1);
            
            Transaction t2 = new Transaction();
            t2.setItem(mouse);
            t2.setType(Transaction.TransactionType.OUT);
            t2.setQuantity(3);
            t2.setNotes("Sold to customer");
            t2.setDeviceId("DEMO");
            t2.setTimestamp(LocalDateTime.now().minusDays(2));
            transactionRepository.save(t2);
            
            System.out.println("[OK] Demo data loaded successfully!");
            System.out.println("[INFO] Items: " + itemRepository.count());
            System.out.println("📝 Transactions: " + transactionRepository.count());
        }
    }
}
