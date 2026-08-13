package dev.chanhne.giftcode.config;

import dev.chanhne.giftcode.core.GiftCode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class Config_GiftCode {
    private final File file;
    private final FileConfiguration config;
    private final Map<String, GiftCode> giftCodes;

    public Config_GiftCode(File file, FileConfiguration config) {
        this.file = file;
        this.config = config;
        this.giftCodes = new HashMap<>();
        loadGiftCodes();  // Load gift codes từ config khi khởi tạo
    }

    // Đọc dữ liệu từ Giftcode.yml
    private void loadGiftCodes() {
        // Nếu chưa có "giftcodes", tạo section
        if (!config.isConfigurationSection("giftcodes")) {
            config.createSection("giftcodes");
            saveConfig();
            return;
        }

        for (String codeKey : Objects.requireNonNull(config.getConfigurationSection("giftcodes")).getKeys(false)) {

            List<UUID> redeemedPlayers = new ArrayList<>();
            List<ItemStack> items = new ArrayList<>();
            int uses = config.getInt("giftcodes." + codeKey + ".uses");
            long createGiftCode = config.getLong("giftcodes." + codeKey + ".createGiftCode");
            double vaultMoney = config.getDouble("giftcodes." + codeKey + ".vault.money", 0);
            double shardMoney = config.getDouble("giftcodes." + codeKey + ".shard.money", 0);
            boolean active = config.getBoolean("giftcodes." + codeKey + ".active", true);
            boolean vaultEnabled = config.getBoolean("giftcodes." + codeKey + ".vault.enabled", false);
            boolean shardEnabled = config.getBoolean("giftcodes." + codeKey + ".shard.enabled", false);
            String permission = config.getString("giftcodes." + codeKey + ".permission","");
            List<String> redeemedStr = config.getStringList("giftcodes." + codeKey + ".redeemedPlayers");
            List<String> commands = config.getStringList("giftcodes." + codeKey + ".commands");
            for (Object obj : config.getList("giftcodes." + codeKey + ".items", new ArrayList<>())) if (obj instanceof ItemStack) {items.add((ItemStack) obj);}
            for (String s : redeemedStr) redeemedPlayers.add(UUID.fromString(s));

            GiftCode giftCode = new GiftCode(
                    codeKey,
                    uses,
                    createGiftCode,
                    active,
                    redeemedPlayers,
                    commands,
                    vaultEnabled,
                    vaultMoney,
                    shardEnabled,
                    shardMoney,
                    items,
                    permission
            );
            giftCodes.put(codeKey, giftCode);
        }
    }

    // Tạo giftcode
    public void createGiftCode(String code,int uses,long createGiftCode,double vault,double shard,List<String> commands,List<ItemStack> items) {
        GiftCode giftCode = new GiftCode(
                code,
                uses,
                createGiftCode,
                true,
                new ArrayList<>(),
                commands,
                vault > 0,
                vault,
                shard > 0,
                shard,
                items,
                ""
        );
        giftCodes.put(code, giftCode);
        // Ghi vào config
        String path = "giftcodes." + code;
        config.set(path + ".uses", uses);
        config.set(path + ".createGiftCode", createGiftCode);
        config.set(path + ".active", true);
        config.set(path + ".redeemedPlayers", new ArrayList<>());
        config.set(path + ".commands", commands);
        config.set(path + ".permission", giftCode.getPermission());
        config.set(path + ".vault.enabled", vault > 0);
        config.set(path + ".vault.money", vault);
        config.set(path + ".shard.enabled", shard > 0);
        config.set(path + ".shard.money", shard);
        config.set(path + ".items", items);
        config.set(path + ".shard.enabled", shard > 0);
        config.set(path + ".shard.money", shard);
        config.set(path + ".items", items);
        saveConfig();
    }

    public List<String> createRandomCodes(int quantity) {
        List<String> generatedCodes = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            String randomCode = generateRandomCode();
            createGiftCode(
                    randomCode,
                    1,
                    0,
                    0D,
                    0D,
                    new ArrayList<>(),
                    new ArrayList<>()
            );
            generatedCodes.add(randomCode);
        }

        return generatedCodes;
    }

    // Hàm tạo chuỗi random
    public String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12; i++) sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
            code = sb.toString();
        } while (giftCodes.containsKey(code));
        return code;
    }

    // Xóa giftcode
    public void deleteGiftCode(String code) {
        giftCodes.remove(code);
        config.set("giftcodes." + code, null);
        saveConfig();
    }

    // Bật giftcode
    public void enableGiftCode(String code) {
        GiftCode giftCode = giftCodes.get(code);
        if (giftCode == null) return;
        giftCode.setActive(true);
        config.set("giftcodes." + code + ".active", true);
        saveConfig();
    }

    // Tắt giftcode
    public void disableGiftCode(String code) {
        GiftCode giftCode = giftCodes.get(code);
        if (giftCode == null) return;
        giftCode.setActive(false);
        config.set("giftcodes." + code + ".active", false);
        saveConfig();
    }

    // Tính thời hạn createGiftCode dựa trên cú pháp (d/h/m)
    public long parseTime(String timeString) {
        try {
            long now = System.currentTimeMillis();
            if (timeString.endsWith("d")) {
                int days = Integer.parseInt(timeString.replace("d", ""));
                return now + days * 86400000L;
            } else if (timeString.endsWith("h")) {
                int hours = Integer.parseInt(timeString.replace("h", ""));
                return now + hours * 3600000L;
            } else if (timeString.endsWith("m")) {
                int minutes = Integer.parseInt(timeString.replace("m", ""));
                return now + minutes * 60000L;
            }
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("Không thể parse thời gian: " + timeString);
        }
        return 0; // 0 = không giới hạn
    }

    // Điều chỉnh quyền hạn cho Gifcode
    public void setPermission(String code, String permission) {
        GiftCode giftCode = giftCodes.get(code);
        if (giftCode == null) return;

        giftCode.setPermission(permission);

        String path = "giftcodes." + code;
        config.set(path + ".permission", permission);

        saveConfig();
    }

    // Lưu file config
    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save Giftcode.yml",e);
        }
    }

    // Reload config
    public void reload() {
        File configFile = new File("plugins/GiftCode", "Giftcode.yml");
        File parentDir = configFile.getParentFile();
        if (!parentDir.exists()) {
            boolean success = parentDir.mkdirs();
            if (!success) {
                Bukkit.getLogger().severe("Không thể tạo thư mục: " + parentDir.getAbsolutePath());
                return;
            }
        }

        try {
            config.load(configFile);
            giftCodes.clear();
            loadGiftCodes();
        } catch (Exception e) {
            long now = System.currentTimeMillis();
            File oldFile = new File("plugins/GiftCode", "config_" + now + ".yml");

            boolean renamed = configFile.renameTo(oldFile);
            if (!renamed) {
                Bukkit.getLogger().warning("Không thể đổi tên file " + configFile.getName() + " thành " + oldFile.getName());
            }

            try {
                if (!configFile.createNewFile()) {
                    Bukkit.getLogger().warning("The config file already exists: " + configFile.getAbsolutePath());
                }
                config.load(configFile);
            } catch (IOException | org.bukkit.configuration.InvalidConfigurationException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Could not create or load the config file: " + configFile.getAbsolutePath(), ex);
            }

            giftCodes.clear();
        }
    }

    public void updateGiftCodeInConfig(String code) {
        GiftCode giftCode = giftCodes.get(code);
        if (giftCode == null) return;

        config.set("giftcodes." + code + ".uses", giftCode.getMaxUses());

        List<String> redeemedUUIDs = new ArrayList<>();
        for (UUID uuid : giftCode.getRedeemedPlayers()) {
            redeemedUUIDs.add(uuid.toString());
        }

        config.set("giftcodes." + code + ".redeemedPlayers", redeemedUUIDs);
        saveConfig();
    }

    // Lấy map giftCodes
    public Map<String, GiftCode> getGiftCodes() {
        return giftCodes;
    }

    public GiftCode getGiftCode(String code) {
        return giftCodes.get(code);
    }

    public boolean exists(String code) {
        return giftCodes.containsKey(code);
    }
}