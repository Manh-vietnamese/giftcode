package dev.chanhne.giftcode.economy;

import org.bukkit.entity.Player;

import dev.chanhne.economy.api.EconomyAPI;
import dev.chanhne.economy.api.ShardAPI;

public class EconomyProvider {

    // ==================== MONEY ====================

    public double getMoney(Player player) {
        return EconomyAPI.getBalance(player);
    }

    public boolean hasMoney(Player player, double amount) {
        return EconomyAPI.has(player, amount);
    }

    public void addMoney(Player player, double amount) {
        if (amount > 0) {
            EconomyAPI.deposit(player, amount, "economy.giftcode-reward");
        }
    }

    public boolean takeMoney(Player player, double amount) {
        return amount > 0
                && EconomyAPI.withdraw(player, amount, "economy.giftcode-reward");
    }

    // ==================== SHARD ====================

    public double getShard(Player player) {
        return ShardAPI.getBalance(player);
    }

    public boolean hasShard(Player player, double amount) {
        return ShardAPI.has(player, amount);
    }

    public void addShard(Player player, double amount) {
        if (amount > 0) {
            ShardAPI.add(player, amount);
        }
    }

    public boolean takeShard(Player player, double amount) {
        return amount > 0 && ShardAPI.take(player, amount);
    }
}