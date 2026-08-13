package dev.chanhne.giftcode.listener;

import dev.chanhne.giftcode.Main_GiftCode;
import dev.chanhne.giftcode.builder.GiftCodeBuilder;
import dev.chanhne.giftcode.config.Config_GiftCode;
import dev.chanhne.giftcode.core.NumberFomat;
import dev.chanhne.giftcode.gui.CreateGiftCodeGUI;
import dev.chanhne.giftcode.gui.RewardItemsGUI;
import dev.chanhne.giftcode.holder.CreateGiftCodeHolder;
import dev.chanhne.giftcode.holder.RewardItemsHolder;
import dev.chanhne.giftcode.manager.BuilderManager;
import dev.chanhne.giftcode.manager.ChatInputManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.wesjd.anvilgui.AnvilGUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import de.rapha149.signgui.SignGUI;

public class CreateGiftCodeListener implements Listener {

    private final Config_GiftCode configGiftCode;
    private final Main_GiftCode plugin;
    private final BuilderManager builderManager;
    private final ChatInputManager chatInputManager;

    public CreateGiftCodeListener(Main_GiftCode plugin, Config_GiftCode configGiftCode, BuilderManager builderManager, ChatInputManager chatInputManager) {
        this.plugin = plugin;
        this.configGiftCode = configGiftCode;
        this.builderManager = builderManager;
        this.chatInputManager = chatInputManager;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof CreateGiftCodeHolder)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CreateGiftCodeHolder)) return;
        if (event.getClickedInventory() == null) return;

        event.setCancelled(true);

        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        if (event.isShiftClick() || event.getClick().isKeyboardClick() || event.getClick().isCreativeAction()) return;

        Player player = (Player) event.getWhoClicked();
        GiftCodeBuilder builder = builderManager.get(player.getUniqueId());

        if (builder == null) return;

        switch (event.getRawSlot()) {
            case 1:
                builder.setVaultEnabled(!builder.isVaultEnabled());
                if (!builder.isVaultEnabled()) builder.setVault(0);

                new CreateGiftCodeGUI(plugin).open(player, builder);
                break;

            case 2:
                builder.setShardEnabled(!builder.isShardEnabled());
                if (!builder.isShardEnabled())  builder.setShard(0);

                new CreateGiftCodeGUI(plugin).open(player, builder);
                break;

            case 3:
                if (builder.isRandomCode()) {
                    plugin.getMessageManager().send(player, "giftcode.random.locked");
                    return;
                }

                openAnvil(player,"GiftCode",builder.getCode(),builder::setCode,builder);
                break;

            case 4:
                openAnvil(player, "Uses", String.valueOf(builder.getUses()),
                    text -> {
                        try {builder.setUses(Integer.parseInt(text));}
                        catch (NumberFormatException e) {player.sendMessage("§cUses không hợp lệ.");}
                    },
                    builder);
                break;

            case 5:
                openAnvil(player, "Expire", builder.getExpireAt() == 0 ? "" : NumberFomat.formatDuration(builder.getExpireAt()),
                    text -> {
                        try {builder.setExpireAt(NumberFomat.parseDuration(text));}
                        catch (Exception e) {player.sendMessage("§cThời gian không hợp lệ.");}
                    },
                    builder);
                break;

            case 10:
                if (!builder.isVaultEnabled()) {
                    plugin.getMessageManager().send(player, "gui.create.vault.disabled-message");
                    return;
                }

                player.closeInventory();
                openNumber(player, "Money", builder.getVault(), builder::setVault, builder);
                break;

            case 11:
                if (!builder.isShardEnabled()) {
                    plugin.getMessageManager().send(player, "gui.create.shard.disabled-message");
                    return;
                }

                player.closeInventory();
                openNumber(player, "Shard", builder.getShard(), builder::setShard, builder);
                break;

            case 12:
                new RewardItemsGUI().open(player, builder);
                break;

            case 14:
                chatInputManager.start(player.getUniqueId(), ChatInputManager.Type.COMMAND);
                player.closeInventory();
                player.sendMessage("");
                player.sendMessage("§bNhập command.");
                player.sendMessage("§7Ví dụ:");
                player.sendMessage("§egive {player} diamond 64");
                player.sendMessage("");
                player.sendMessage("§7cancel để hủy.");
                break;

            case 20:
                builderManager.remove(player.getUniqueId());
                player.closeInventory();
                player.sendMessage("§cĐã hủy tạo GiftCode.");
                break;

            case 24:
                if (builder.isRandomCode()) {
                    List<String> codes = new ArrayList<>();
                    for (int i = 0; i < builder.getRandomAmount(); i++) {
                        String code = configGiftCode.generateRandomCode();
                        configGiftCode.createGiftCode(
                                code,
                                builder.getUses(),
                                builder.getExpireAt(),
                                builder.getVault(),
                                builder.getShard(),
                                new ArrayList<>(builder.getCommands()),
                                new ArrayList<>(builder.getItems())
                        );
                        codes.add(code);
                    }

                    player.closeInventory();
                    builderManager.remove(player.getUniqueId());
                    plugin.getMessageManager().send(player, "giftcode.random_created", "amount",
                            String.valueOf(codes.size())
                    );

                    for (String code : codes) {player.sendMessage(
                                plugin.getMessageManager().get(player, "giftcode.created", "code", code)
                                        .clickEvent(ClickEvent.copyToClipboard(code))
                                        .hoverEvent(HoverEvent.showText(Component.text("Click để sao chép", NamedTextColor.YELLOW)
                    )));}

                } else {
                    configGiftCode.createGiftCode(
                            builder.getCode(),
                            builder.getUses(),
                            builder.getExpireAt(),
                            builder.getVault(),
                            builder.getShard(),
                            new ArrayList<>(builder.getCommands()),
                            new ArrayList<>(builder.getItems())
                    );

                    player.closeInventory();
                    builderManager.remove(player.getUniqueId());

                    player.sendMessage(plugin.getMessageManager().get(player, "giftcode.created", "code", builder.getCode())
                                    .clickEvent(ClickEvent.copyToClipboard(builder.getCode()))
                                    .hoverEvent(HoverEvent.showText(Component.text("Click để sao chép", NamedTextColor.YELLOW)
                    )));
                }

                if (builder.getExpireAt() == 0) {
                    plugin.getMessageManager().send(player, "giftcode.no_expiry");
                } else {
                    plugin.getMessageManager().send(player, "giftcode.expiry", "expiry",
                            NumberFomat.formatDuration(builder.getExpireAt())
                    );
                }

                return;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof RewardItemsHolder)) return;

        Player player = (Player) event.getPlayer();
        GiftCodeBuilder builder = builderManager.get(player.getUniqueId());

        if (builder == null) return;
        builder.getItems().clear();

        for (int i = 0; i < 27; i++) {
            ItemStack item = event.getInventory().getItem(i);
            if (item == null || item.getType().isAir()) continue;
            builder.getItems().add(item.clone());
        }
        player.getScheduler().runDelayed(plugin, task -> new CreateGiftCodeGUI(plugin).open(player, builder), null, 1L);
    }

    private void openNumber(Player player, String title, double value, Consumer<Double> consumer, GiftCodeBuilder builder) {
        try {
            SignGUI.builder().setLines("", title, "Supports: k m b t", "")
                    .setHandler((p, result) -> {
                        try {
                            double number = NumberFomat.parseNumber(result.getLineWithoutColor(0).trim());
                            consumer.accept(number);
                        } catch (NumberFormatException e) {
                            p.sendMessage("§cInvalid number.");
                        }
                        player.getScheduler().runDelayed(plugin, task -> new CreateGiftCodeGUI(plugin).open(player, builder), null, 1L);
                        return Collections.emptyList();
                    })
                    .build()
                    .open(player);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openAnvil(Player player, String title, String value, Consumer<String> consumer, GiftCodeBuilder builder) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title(title)
                .text(value == null ? "" : value)
                .onClick((slot, state) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }

                    consumer.accept(state.getText());

                    return Arrays.asList(
                            AnvilGUI.ResponseAction.close(),
                            AnvilGUI.ResponseAction.run(() ->
                                    player.getScheduler().run(
                                            plugin,
                                            task -> new CreateGiftCodeGUI(plugin).open(player, builder),
                                            null
                                    )
                            )
                    );
                })
                .open(player);
    }
}