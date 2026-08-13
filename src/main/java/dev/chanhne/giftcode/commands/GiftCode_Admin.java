package dev.chanhne.giftcode.commands;

import dev.chanhne.giftcode.config.Config_GiftCode;
import dev.chanhne.giftcode.Main_GiftCode;
import dev.chanhne.giftcode.core.GiftCode;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GiftCode_Admin implements CommandExecutor {

    private final Main_GiftCode plugin;
    private final Config_GiftCode configGiftCode;

    public GiftCode_Admin(Main_GiftCode plugin, Config_GiftCode configGiftCode) {
        this.plugin = plugin;
        this.configGiftCode = configGiftCode;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        String codeName = args[0];
        GiftCode giftCode = configGiftCode.getGiftCodes().get(codeName);
        UUID playerId = player.getUniqueId();


        // Kiểm tra quyền
        if (!sender.hasPermission("giftcode.use")) {
            plugin.getMessageManager().send(sender, "no_permission");
            return true;
        }

        if (args.length < 1) {
            plugin.getMessageManager().send(player, "code_user");
            return true;
        }

        if (giftCode == null) {
            plugin.getMessageManager().send(player, "giftcode.not_found", "code", codeName);
            return true;
        }

        if (!giftCode.canUse(player)) {
            plugin.getMessageManager().send(player, "no_giftcode");
            return true;
        }

        int required = getRequiredSlots(giftCode.getItems());
        int free = getFreeSlots(player);

        if (free < required) {
            int missing = required - free;
            player.sendMessage("§cBạn cần trống thêm §e" + missing + " §cô trong túi đồ để nhận GiftCode.");
            return true;
        }

        // Money
        if (giftCode.isVaultEnabled() && giftCode.getVaultMoney() > 0) {
            plugin.getEconomyProvider().addMoney(player, giftCode.getVaultMoney());
        }

        // Shard
        if (giftCode.isShardEnabled() && giftCode.getShardMoney() > 0) {
            plugin.getEconomyProvider().addShard(player, giftCode.getShardMoney());
        }

        // Commands
        for (String cmd : giftCode.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()));
        }

        // Items
        for (ItemStack item : giftCode.getItems()) {
            HashMap<Integer, ItemStack> remain = player.getInventory().addItem(item.clone());
            for (ItemStack drop : remain.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }

        giftCode.redeem(playerId);
        configGiftCode.updateGiftCodeInConfig(codeName);
        plugin.getMessageManager().send(player, "giftcode.redeemed", "code", codeName);
        if (giftCode.getMaxUses() <= 0) configGiftCode.deleteGiftCode(codeName);

        return true;
    }

    private int getRequiredSlots(List<ItemStack> items) {
        int slots = 0;

        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) continue;
            slots++;
        }
        return slots;
    }

    private int getFreeSlots(Player player) {
        int free = 0;

        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir()) free++;
        }
        return free;
    }
}