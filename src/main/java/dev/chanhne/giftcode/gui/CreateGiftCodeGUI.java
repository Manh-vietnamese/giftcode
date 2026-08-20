package dev.chanhne.giftcode.gui;

import dev.chanhne.giftcode.Mainplugin;
import dev.chanhne.giftcode.builder.GiftCodeBuilder;
import dev.chanhne.giftcode.core.NumberFomat;
import dev.chanhne.giftcode.holder.CreateGiftCodeHolder;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class CreateGiftCodeGUI {

    private static final int SIZE = 27;
    private final Mainplugin plugin;

    public CreateGiftCodeGUI(Mainplugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, GiftCodeBuilder builder) {
        Inventory inv = Bukkit.createInventory(new CreateGiftCodeHolder(), SIZE, Component.text("§8Create GiftCode"));

        inv.setItem(1, createToggle(builder.isVaultEnabled(), "Vault"));
        inv.setItem(2, createToggle(builder.isShardEnabled(), "Shard"));

        if (builder.isRandomCode()) {
            inv.setItem(3, createButton(
                    Material.NAME_TAG,
                    plugin.getMessageManager().get(player, "gui.create.code.name"),
                    plugin.getMessageManager().getList(player, "gui.create.code.random")));

        } else {
            inv.setItem(3, createButton(
                    Material.NAME_TAG,
                    plugin.getMessageManager().get(player, "gui.create.code.name"),
                    plugin.getMessageManager().getList(player, "gui.create.code.lore", "code", builder.getCode())));
        }

        inv.setItem(4, createButton(
                Material.PLAYER_HEAD,
                plugin.getMessageManager().get(player, "gui.create.uses.name"),
                plugin.getMessageManager().getList(player,
                        "gui.create.uses.lore",
                        "uses", String.valueOf(builder.getUses()))));

        inv.setItem(5, createButton(
                Material.CLOCK,
                plugin.getMessageManager().get(player, "gui.create.expire.name"),
                plugin.getMessageManager().getList(player,
                        "gui.create.expire.lore",
                        "expire",
                        builder.getExpireAt() == 0 ? "Never" : NumberFomat.formatDuration(builder.getExpireAt()))));

        inv.setItem(10, createButton(
                builder.isVaultEnabled() ? Material.EMERALD : Material.GRAY_DYE,
                plugin.getMessageManager().get(player, "gui.create.vault.name"),
                builder.isVaultEnabled()
                        ? plugin.getMessageManager().getList(player,
                                "gui.create.vault.lore",
                                "vault", NumberFomat.formatNumber(builder.getVault()))
                        : plugin.getMessageManager().getList(
                                player,
                                "gui.create.vault.disabled")));

        inv.setItem(11, createButton(
                builder.isShardEnabled() ? Material.AMETHYST_SHARD : Material.GRAY_DYE,
                plugin.getMessageManager().get(player, "gui.create.shard.name"),
                builder.isShardEnabled()
                        ? plugin.getMessageManager().getList(player,
                                "gui.create.shard.lore",
                                "shard", NumberFomat.formatNumber(builder.getShard()))
                        : plugin.getMessageManager().getList(player,
                                "gui.create.shard.disabled")));

        inv.setItem(12, createButton(
                Material.CHEST,
                plugin.getMessageManager().get(player, "gui.create.items.name"),
                plugin.getMessageManager().getList(player,
                        "gui.create.items.lore",
                     "items", String.valueOf(builder.getItems().size()))));

        inv.setItem(14, createButton(
                Material.WRITABLE_BOOK,
                plugin.getMessageManager().get(player, "gui.create.commands.name"),
                plugin.getMessageManager().getList(player,
                        "gui.create.commands.lore",
                        "commands", String.valueOf(builder.getCommands().size()))));

        inv.setItem(16, createInfo(builder));

        inv.setItem(20, createButton(
                Material.RED_STAINED_GLASS_PANE,
                plugin.getMessageManager().get(player, "gui.create.cancel.name"),
                plugin.getMessageManager().getList(player,"gui.create.cancel.lore")));

        inv.setItem(24, createButton(
                Material.LIME_STAINED_GLASS_PANE,
                plugin.getMessageManager().get(player, "gui.create.confirm.name"),
                plugin.getMessageManager().getList(player,"gui.create.confirm.lore")));

        player.openInventory(inv);
    }

    private ItemStack createButton(Material mat, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(name);
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfo(GiftCodeBuilder builder) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(Component.text("§bInformation"));
        meta.lore(Arrays.asList(
                Component.text("§7Code: §f" + (builder.isRandomCode() ? "Random" : builder.getCode())),
                Component.text("§7Uses: §e" + builder.getUses()),
                Component.text("§7Expire: " + (builder.getExpireAt() == 0 ? "§aNever" : "§e" + NumberFomat.formatDuration(builder.getExpireAt()))),
                Component.empty(),
                Component.text("§7Vault: §6$" + NumberFomat.formatNumber(builder.getVault())),
                Component.text("§7Shard: §d" + NumberFomat.formatNumber(builder.getShard())),
                Component.empty(),
                Component.text("§7Items: §a" + builder.getItems().size()),
                Component.text("§7Commands: §b" + builder.getCommands().size())
        ));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createToggle(boolean enabled, String name) {
        ItemStack item = new ItemStack(enabled ? Material.LIME_WOOL : Material.GRAY_WOOL);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§6" + name));
        meta.lore(Arrays.asList(Component.text("§7Status: " + (enabled ? "§aEnabled" : "§cDisabled"))));

        item.setItemMeta(meta);
        return item;
    }
}