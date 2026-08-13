package dev.chanhne.giftcode;

import dev.chanhne.giftcode.commands.GiftCode_Admin;
import dev.chanhne.giftcode.commands.GiftCode_Redeem;
import dev.chanhne.giftcode.commands.GiftCode_Completer;
import dev.chanhne.giftcode.config.Config_GiftCode;
import dev.chanhne.giftcode.core.Messager;
import dev.chanhne.giftcode.economy.EconomyProvider;
import dev.chanhne.giftcode.listener.CreateGiftCodeListener;
import dev.chanhne.giftcode.manager.BuilderManager;
import dev.chanhne.giftcode.manager.ChatInputManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Objects;

public class Main_GiftCode extends JavaPlugin {

    private Messager messageManager;
    private EconomyProvider shardProvider;

    @Override
    public void onEnable() {

        // saveDefaultConfig();
        saveResource("messages.yml", false);
        messageManager = new Messager(this);
        shardProvider = new EconomyProvider();

        // Lấy file cấu hình của plugin
        File file = new File(getDataFolder(), "Giftcode.yml");
        if (!file.exists()) saveResource("Giftcode.yml", false);

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        Config_GiftCode configGiftCode = new Config_GiftCode(file, config);
        BuilderManager builderManager = new BuilderManager();
        ChatInputManager chatInputManager = new ChatInputManager();

        getServer().getPluginManager().registerEvents(new CreateGiftCodeListener(this, configGiftCode, builderManager, chatInputManager),this);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
            event -> {
                // Đăng ký lệnh
                Objects.requireNonNull(getCommand("gc")).setExecutor(new GiftCode_Redeem(this, configGiftCode, builderManager));
                Objects.requireNonNull(getCommand("code")).setExecutor(new GiftCode_Admin(this, configGiftCode));
                Objects.requireNonNull(getCommand("gc")).setTabCompleter(new GiftCode_Completer(configGiftCode));
            }
        );

        getLogger().info("GiftCode Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("GiftCode Plugin disabled!");
    }

    public Messager getMessageManager() {return messageManager;}
    public EconomyProvider getEconomyProvider() {return shardProvider;}
}