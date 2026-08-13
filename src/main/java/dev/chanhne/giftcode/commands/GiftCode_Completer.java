package dev.chanhne.giftcode.commands;

import dev.chanhne.giftcode.config.Config_GiftCode;
import dev.chanhne.giftcode.core.GiftCode;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GiftCode_Completer implements TabCompleter {

    private final Config_GiftCode configGiftCode;

    public GiftCode_Completer(Config_GiftCode configGiftCode) {
        this.configGiftCode = configGiftCode;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        // Nếu lệnh không phải /gc thì bỏ qua
        if (!command.getName().equalsIgnoreCase("gc")) return null;

        // Gợi ý subcommand cho arg[0]
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            // Các sub-lệnh có sẵn:
            suggestions.add("create");
            suggestions.add("info");
            suggestions.add("delete");
            suggestions.add("reload");
            suggestions.add("enable");
            suggestions.add("disable");
            suggestions.add("list");
            suggestions.add("permission");
            return partialMatch(suggestions, args[0]);
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length == 2) return partialMatch(Arrays.asList("random","<code>"), args[1]);
            if (args.length == 3) return partialMatch(Arrays.asList("1","5","10","50","100","[Count]"), args[2]);
            if (args.length == 4) return partialMatch(Arrays.asList("0","30m","1h","1d","7d","30d","[Time]"), args[3]);
        }

        if (args[0].equalsIgnoreCase("info") && args.length == 2) {
            return partialMatch(new ArrayList<>(configGiftCode.getGiftCodes().keySet()), args[1]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();

            if (sub.equals("delete")
                    || sub.equals("info")
                    || sub.equals("enable")
                    || sub.equals("disable")
                    || sub.equals("permission")
                    || sub.equals("list")
                    || sub.equals("reload")) {
                return partialMatch(new ArrayList<>(configGiftCode.getGiftCodes().keySet()),args[1]);
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("permission")) {
            return partialMatch(Arrays.asList("add", "delete", "list"),args[2]);
        }

        if (args[0].equalsIgnoreCase("info") && args.length == 3) {
            return partialMatch(Arrays.asList("items", "commands"), args[2]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("permission") && args[2].equalsIgnoreCase("add")) {
            GiftCode giftCode = configGiftCode.getGiftCode(args[1]);
            List<String> suggestions = new ArrayList<>();
            if (giftCode != null && giftCode.getPermission() != null && !giftCode.getPermission().isBlank()) suggestions.add(giftCode.getPermission());
            return partialMatch(suggestions, args[3]);
        }
        return null;
    }

    // Hàm hỗ trợ lọc kết quả gợi ý theo phần đã nhập
    private List<String> partialMatch(List<String> source, String input) {
        if (input == null || input.isEmpty()) return source;

        List<String> matches = new ArrayList<>();
        String lower = input.toLowerCase();
        for (String s : source) {
            if (s.toLowerCase().startsWith(lower)) {
                matches.add(s);
            }
        }
        return matches;
    }
}
