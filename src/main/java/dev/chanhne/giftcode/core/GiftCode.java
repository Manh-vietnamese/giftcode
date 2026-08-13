package dev.chanhne.giftcode.core;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiftCode {

    private final List<UUID> redeemedPlayers;
    private final List<String> commands;
    private final List<ItemStack> items;
    private final String code;
    private final boolean shardEnabled;
    private final double shardMoney;
    private boolean active;
    private boolean vaultEnabled;
    private double vaultMoney;
    private int maxUses;
    private long Time_user;
    private String permission;

    public GiftCode(
            String code,
            int maxUses,
            long timeUser,
            boolean active,
            List<UUID> redeemedPlayers,
            List<String> commands,
            boolean vaultEnabled,
            double vaultMoney,
            boolean shardEnabled,
            double shardMoney,
            List<ItemStack> items,
            String permission) {

        this.code = code;
        this.maxUses = maxUses;
        this.Time_user = timeUser;
        this.active = active;
        this.redeemedPlayers = redeemedPlayers;
        this.commands = commands;
        this.vaultEnabled = vaultEnabled;
        this.vaultMoney = vaultMoney;
        this.shardEnabled = shardEnabled;
        this.shardMoney = shardMoney;
        this.items = items;
        this.permission = permission;
    }

    public void setMaxUses(int maxUses) {this.maxUses = maxUses;}
    public void setActive(boolean active) {this.active = active;}
    public void setExpireAt(long expireAt) {this.Time_user = expireAt;}
    public void setPermission(String permission) {this.permission = permission;}
    public boolean isActive() {return active;}
    public boolean isVaultEnabled() {return vaultEnabled;}
    public boolean isShardEnabled() {return shardEnabled;}
    public List<UUID> getRedeemedPlayers() {return redeemedPlayers;}
    public List<String> getCommands() {return commands;}
    public List<ItemStack> getItems() {return items;}
    public double getVaultMoney() {return vaultMoney;}
    public double getShardMoney() {return shardMoney;}
    public String getCode() {return code;}
    public int getMaxUses() {return maxUses;}
    public long getExpireAt() {return Time_user;}
    public String getPermission() {return permission;}

    // Kiểm tra còn hạn và chưa quá số lần sử dụng
    public boolean canUse(Player player) {
        if (!active) return false; // Nếu giftcode đã tắt -> không dùng
        if (Time_user > 0 && System.currentTimeMillis() > Time_user) {
            // Nếu đã hết thời gian
            setActive(false);  // Vô hiệu hóa giftcode
            return false;
        }

        if (permission != null && !permission.isBlank() && !player.hasPermission(permission)) return false;
        if (redeemedPlayers.contains(player.getUniqueId())) return false; // Mỗi người chơi chỉ được nhận 1 lần
        if (maxUses <= 0) return false; // Kiểm tra số lần sử dụng còn lại

        return true;
    }

    // Người chơi sử dụng giftcode
    public void redeem(UUID playerId) {
        redeemedPlayers.add(playerId);
        maxUses--; // Mỗi lần dùng trừ 1
    }
}
