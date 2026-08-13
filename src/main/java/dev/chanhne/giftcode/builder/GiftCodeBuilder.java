package dev.chanhne.giftcode.builder;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GiftCodeBuilder {

    private int uses;
    private int randomAmount = 1;
    private long expireAt;
    private double vault;
    private double shard;
    private boolean vaultEnabled = true;
    private boolean shardEnabled = true;
    private boolean randomCode;
    private String code;
    private final List<ItemStack> items = new ArrayList<>();
    private final List<String> commands = new ArrayList<>();

    public GiftCodeBuilder(String code, int uses, long expireAt) {
        this.code = code;
        this.uses = uses;
        this.expireAt = expireAt;
    }

    public boolean isVaultEnabled() {return vaultEnabled;}
    public boolean isShardEnabled() {return shardEnabled;}
    public boolean isRandomCode() {return randomCode;}
    public double getVault() {return vault;}
    public double getShard() {return shard;}
    public void setUses(int uses) {this.uses = uses;}
    public void setCode(String code) {this.code = code;}
    public void setVault(double vault) {this.vault = vault;}
    public void setShard(double shard) {this.shard = shard;}
    public void setExpireAt(long expireAt) {this.expireAt = expireAt;}
    public void setVaultEnabled(boolean vaultEnabled) {this.vaultEnabled = vaultEnabled;}
    public void setShardEnabled(boolean shardEnabled) {this.shardEnabled = shardEnabled;}
    public void setRandomCode(boolean randomCode) {this.randomCode = randomCode;}
    public void setRandomAmount(int randomAmount) {this.randomAmount = randomAmount;}
    public List<ItemStack> getItems() {return items;}
    public List<String> getCommands() {return commands;}
    public int getUses() {return uses;}
    public int getRandomAmount() {return randomAmount;}
    public String getCode() {return code;}
    public long getExpireAt() {return expireAt;}
}