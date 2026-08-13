package dev.chanhne.giftcode.commands;

import dev.chanhne.giftcode.config.Config_GiftCode;
import dev.chanhne.giftcode.core.GiftCode;
import dev.chanhne.giftcode.core.NumberFomat;
import dev.chanhne.giftcode.gui.CreateGiftCodeGUI;
import dev.chanhne.giftcode.manager.BuilderManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import dev.chanhne.giftcode.Main_GiftCode;
import dev.chanhne.giftcode.builder.GiftCodeBuilder;

import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GiftCode_Redeem implements CommandExecutor {

    private final Main_GiftCode plugin;
    private final Config_GiftCode configGiftCode;
    private final BuilderManager builderManager;

    public GiftCode_Redeem(Main_GiftCode plugin, Config_GiftCode configGiftCode, BuilderManager builderManager) {
        this.plugin = plugin;
        this.configGiftCode = configGiftCode;
        this.builderManager = builderManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Kiểm tra quyền
        if (!sender.hasPermission("giftcode.admin")) {
            plugin.getMessageManager().send(sender, "no_permission");
            return true;
        }

        if (args.length == 0) {
            plugin.getMessageManager().send(sender, "plugin_user");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(sender, args);
                break;

            case "delete":
                if (args.length < 2) {
                    plugin.getMessageManager().send(sender, "delete_user");
                    return true;
                }
                configGiftCode.deleteGiftCode(args[1]);
                plugin.getMessageManager().send(sender, "giftcode.deleted", "code", args[1]);

                break;
        case "info":
            handleInfo(sender, args);
            return true;

            case "reload":
                configGiftCode.reload();
                plugin.getMessageManager().reload();
                plugin.getMessageManager().send(sender, "reload_plugin");
                break;

            case "enable":
                if (args.length < 2) {
                    plugin.getMessageManager().send(sender, "enable_user");
                    return true;
                }

                configGiftCode.enableGiftCode(args[1]);
                plugin.getMessageManager().send(sender,"giftcode.enableGiftCode","code", args[1]);
                break;

            case "disable":
                if (args.length < 2) {
                    plugin.getMessageManager().send(sender, "disable_user");
                    return true;
                }

                configGiftCode.disableGiftCode(args[1]);
                plugin.getMessageManager().send(sender,"giftcode.disableGiftCode","code", args[1]);
                break;

            case "list":
                plugin.getMessageManager().send(sender, "list_giftcode");
                for (String code : configGiftCode.getGiftCodes().keySet()) {
                    sender.sendMessage("- " + code);
                }

                break;

            case "permission":
                if (args.length < 3) {
                    plugin.getMessageManager().send(sender, "permission.usage");
                    return true;
                }

                GiftCode giftCode = configGiftCode.getGiftCode(args[1]);

                if (giftCode == null) {
                    plugin.getMessageManager().send(sender, "giftcode.not_found");
                    return true;
                }

                switch (args[2].toLowerCase()) {
                    case "add":
                        if (args.length < 4) {
                            plugin.getMessageManager().send(sender, "usage_permission.add");
                            return true;
                        }
                        configGiftCode.setPermission(args[1], args[3]);
                        plugin.getMessageManager().send(sender,"permission.add","code", args[1],"permission", args[3]);
                        break;

                    case "delete":
                        configGiftCode.setPermission(args[1], "");
                        plugin.getMessageManager().send(sender, "permission.delete","code", args[1]);
                        break;

                    case "list":
                        plugin.getMessageManager().send(sender,"permission.list","code", args[1],"permission",
                                giftCode.getPermission().isBlank() ? "-" : giftCode.getPermission());

                        break;
                    default:
                        plugin.getMessageManager().send(sender, "permission.usage");
                        break;
                }

                return true;

            default:
                plugin.getMessageManager().send(sender, "missing_argument");
                break;
        }

        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        boolean random = args[1].equalsIgnoreCase("random");
        String code;
        int uses = 1;
        int amount = 1;
        long expireAt = 0;

        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "create_user");
            return;
        }

        if (!(sender instanceof Player)) {
            plugin.getMessageManager().send(sender, "player_only");
            return;
        }

        if (random) {
            code = configGiftCode.generateRandomCode();
            if (args.length >= 3) uses = Integer.parseInt(args[2]);
            if (args.length >= 4) expireAt = NumberFomat.parseDuration(args[3]);
        } else {
            code = args[1];
            if (configGiftCode.exists(code)) {
                player.sendMessage(plugin.getMessageManager().get(player,
                        "giftcode.already_exists",
                        "code", code));
                return;
            }

            if (args.length >= 3) uses = Integer.parseInt(args[2]);
            if (args.length >= 4) expireAt = NumberFomat.parseDuration(args[3]);
        }

        if (args.length >= 3) {
            try {amount = Integer.parseInt(args[2]);}
            catch (NumberFormatException e) {
                plugin.getMessageManager().send(player, "invalid_number");
                return;
            }
        }

        GiftCodeBuilder builder = new GiftCodeBuilder(code, uses, expireAt);
        builder.setRandomCode(random);
        builder.setRandomAmount(amount);
        builderManager.create(player.getUniqueId(), builder);

        new CreateGiftCodeGUI(plugin).open(player, builder);
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "player_only");
            return;
        }

        if (args.length < 2) {
            plugin.getMessageManager().send(player, "info.usage");
            return;
        }

        GiftCode giftCode = configGiftCode.getGiftCode(args[1]);

        if (giftCode == null) {
            plugin.getMessageManager().send(player,"giftcode.not_found","code", args[1]);
            return;
        }

        // /gc info <code> items
        if (args.length >= 3 && args[2].equalsIgnoreCase("items")) {
            sendItemsInfo(player, giftCode);
            return;
        }

        // /gc info <code> commands
        if (args.length >= 3 && args[2].equalsIgnoreCase("commands")) {
            sendCommandsInfo(player, giftCode);
            return;
        }

        int used = giftCode.getRedeemedPlayers().size();
        int total = used + giftCode.getMaxUses();
        Component header = plugin.getMessageManager().get(player, "info.header");
        Component code = plugin.getMessageManager().get(player, "info.code", "code", giftCode.getCode())
                .clickEvent(ClickEvent.copyToClipboard(giftCode.getCode()))
                .hoverEvent(HoverEvent.showText(plugin.getMessageManager().get(player, "info.hover.copy")));

        Component status = plugin.getMessageManager().get(player,"info.status","status",giftCode.isActive() ? "§aEnabled" : "§cDisabled")
                .clickEvent(ClickEvent.runCommand(giftCode.isActive() ? "/gc disable " + giftCode.getCode() : "/gc enable " + giftCode.getCode()))
                .hoverEvent(HoverEvent.showText(plugin.getMessageManager().get(player,"info.hover.status")));

        Component uses = plugin.getMessageManager().get(player, "info.uses", "used", String.valueOf(used), "max", String.valueOf(total));
        Component expire = plugin.getMessageManager().get(player, "info.expire", "expire", NumberFomat.formatDuration(giftCode.getExpireAt()));
        Component vault = plugin.getMessageManager().get(player, "info.vault", "vault", NumberFomat.formatNumber(giftCode.getVaultMoney()));
        Component shard = plugin.getMessageManager().get(player, "info.shard", "shard", NumberFomat.formatNumber(giftCode.getShardMoney()));
        Component items = plugin.getMessageManager().get(player, "info.items", "items", String.valueOf(giftCode.getItems().size()))
                .clickEvent(ClickEvent.runCommand("/gc info " + giftCode.getCode() + " items"))
                .hoverEvent(HoverEvent.showText(plugin.getMessageManager().get(player, "info.hover.items")));

        Component commands = plugin.getMessageManager().get(player, "info.commands", "commands", String.valueOf(giftCode.getCommands().size()))
                .clickEvent(ClickEvent.runCommand("/gc info " + giftCode.getCode() + " commands"))
                .hoverEvent(HoverEvent.showText(plugin.getMessageManager().get(player, "info.hover.commands")));

        Component permission = plugin.getMessageManager().get(player, "info.permission", "permission",
                giftCode.getPermission() == null || giftCode.getPermission().isBlank() ? "-" : giftCode.getPermission());

        player.sendMessage(header);
        player.sendMessage(code);
        player.sendMessage(status);
        player.sendMessage(uses);
        player.sendMessage(expire);
        player.sendMessage(vault);
        player.sendMessage(shard);
        player.sendMessage(items);
        player.sendMessage(commands);
        player.sendMessage(permission);
        player.sendMessage(plugin.getMessageManager().get(player, "info.footer"));
    }

    private void sendItemsInfo(Player player, GiftCode giftCode) {
        player.sendMessage(plugin.getMessageManager().get(player, "info.footer"));
        player.sendMessage(plugin.getMessageManager().get(player, "info.items_header"));

        if (giftCode.getItems().isEmpty()) {
            player.sendMessage(plugin.getMessageManager().get(player, "info.items_empty"));
            return;
        }

        for (ItemStack item : giftCode.getItems()) {
            if (item == null || item.getType().isAir()) continue;

            ItemMeta meta = item.getItemMeta();
            Component line = Component.text()
                    .append(Component.text("• "))
                    .append(item.effectiveName())
                    .append(Component.text(" x" + item.getAmount()))
                    .build();

            player.sendMessage(line);
            if (meta != null && meta.hasEnchants()) {
                for (Map.Entry<Enchantment, Integer> enchant : meta.getEnchants().entrySet()) {
                    player.sendMessage(Component.text(
                            "   §7- §b"
                            + enchant.getKey().getKey().getKey()
                            + " "
                            + enchant.getValue()
                    ));
                }}
        }

        player.sendMessage(plugin.getMessageManager().get(player, "info.footer"));
    }

    private void sendCommandsInfo(Player player, GiftCode giftCode) {
        player.sendMessage(plugin.getMessageManager().get(player, "info.footer"));
        player.sendMessage(plugin.getMessageManager().get(player, "info.commands_header"));

        if (giftCode.getCommands().isEmpty()) {
            player.sendMessage(plugin.getMessageManager().get(player, "info.commands_empty"));
            player.sendMessage(plugin.getMessageManager().get(player, "info.footer"));
            return;
        }

        int index = 1;
        for (String command : giftCode.getCommands()) {
            player.sendMessage(Component.text(index + ". " + command)
                            .clickEvent(ClickEvent.copyToClipboard(command))
                            .hoverEvent(HoverEvent.showText(plugin.getMessageManager().get(player, "info.hover.copy")
            )));
            index++;
        }

        player.sendMessage(plugin.getMessageManager().get(player, "info.footer"));
    }
}