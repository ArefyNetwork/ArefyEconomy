package com.arefyeconomy.locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles loading and retrieving translatable messages from messages.json
 */
public class Messages {
    private static final Logger LOGGER = Logger.getLogger("ArefyEconomy");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path MESSAGES_PATH = Path.of("mods", "ArefyEconomy", "messages.json");

    private static Map<String, String> messages = new HashMap<>();

    // Default messages (English) - using LinkedHashMap to preserve order in JSON
    private static final Map<String, String> DEFAULTS = createDefaults();

    private static Map<String, String> createDefaults() {
        Map<String, String> m = new LinkedHashMap<>();

        // === CHAT MESSAGES ===
        m.put("chat.prefix", "[Economy]");
        m.put("chat.players_only", "This command can only be used by players");
        m.put("chat.player_data_error", "Error: Could not get your player data");
        m.put("chat.error_load_balance", "Error: Could not load balance");
        m.put("chat.player_ref_error", "Error: Could not get player reference");

        // === BALANCE COMMAND ===
        m.put("balance.header", "----- Your Balance -----");
        m.put("balance.current", "Balance: {amount}");

        // === PAY COMMAND ===
        m.put("pay.usage", "Usage: /pay <player> <amount> or /pay to open GUI");
        m.put("pay.amount_positive", "Amount must be a positive number");
        m.put("pay.minimum_transaction", "Minimum transaction is {amount}");
        m.put("pay.success", "Payment sent! {amount}");
        m.put("pay.success_fee", " (Fee: {fee})");
        m.put("pay.insufficient_funds", "Insufficient funds. Your balance: {balance}");
        m.put("pay.self_transfer", "You cannot send money to yourself");
        m.put("pay.invalid_amount", "Invalid amount");
        m.put("pay.recipient_max_balance", "Recipient has reached maximum balance");
        m.put("pay.transfer_failed", "Transfer failed!");

        // === PAY GUI ===
        m.put("gui.pay.title", "PAY PLAYER");
        m.put("gui.pay.select_player", "SELECT PLAYER");
        m.put("gui.pay.amount", "AMOUNT");
        m.put("gui.pay.your_balance", "Your balance: {balance}");
        m.put("gui.pay.your_balance_label", "Your Balance");
        m.put("gui.pay.search_label", "Search");
        m.put("gui.pay.fee_label", "Transfer Fee");
        m.put("gui.pay.balance_after", "Balance After");
        m.put("gui.pay.insufficient", "Insufficient");
        m.put("gui.pay.send", "SEND PAYMENT");
        m.put("gui.pay.preview", "Send {amount} to {player}");
        m.put("gui.pay.fee_info", "Fee: {fee} ({percent}%)");
        m.put("gui.pay.total", "Total: {total}");
        m.put("gui.pay.enter_amount", "Enter amount to send");
        m.put("gui.pay.select_player_first", "Select a player first");
        m.put("gui.pay.online_players", "ONLINE PLAYERS");
        m.put("gui.pay.no_players", "No other players online");

        // === ADMIN GUI ===
        m.put("gui.admin.title", "ECONOMY ADMIN");
        m.put("gui.admin.tab.dashboard", "DASHBOARD");
        m.put("gui.admin.tab.players", "PLAYERS");
        m.put("gui.admin.tab.top", "TOP BALANCES");
        m.put("gui.admin.tab.log", "LOG");
        m.put("gui.admin.tab.config", "CONFIG");
        m.put("gui.admin.force_save", "FORCE SAVE");
        m.put("gui.admin.reload_config", "RELOAD CONFIG");

        // === DASHBOARD ===
        m.put("gui.dashboard.economy_stats", "ECONOMY STATISTICS");
        m.put("gui.dashboard.total_circulating", "Total Circulating");
        m.put("gui.dashboard.players_with_balance", "Total Players");
        m.put("gui.dashboard.avg_balance", "Average Balance");
        m.put("gui.dashboard.current_config", "Current Configuration");
        m.put("gui.dashboard.max_balance", "Max Balance:");
        m.put("gui.dashboard.transfer_fee", "Transfer Fee:");
        m.put("gui.dashboard.auto_save", "Auto-Save:");
        m.put("gui.dashboard.recent_activity", "RECENT ACTIVITY");
        m.put("gui.dashboard.no_activity", "No recent activity");

        // === PLAYER MANAGEMENT ===
        m.put("gui.players.search", "Search player...");
        m.put("gui.players.search_label", "Search");
        m.put("gui.players.select_player", "Select a player");
        m.put("gui.players.amount", "Amount:");
        m.put("gui.players.give", "GIVE");
        m.put("gui.players.take", "TAKE");
        m.put("gui.players.set", "SET");
        m.put("gui.players.reset", "RESET");
        m.put("gui.players.reset_confirm", "OK?");
        m.put("gui.players.balance", "Balance: {amount}");
        m.put("gui.players.online", "Online");
        m.put("gui.players.offline", "Offline");
        m.put("gui.players.no_players", "No players found");
        m.put("gui.players.page", "Page {current} of {total}");
        m.put("gui.players.selected", "Selected");

        // === CONFIG GUI ===
        m.put("gui.config.title", "ECONOMY CONFIGURATION");
        m.put("gui.config.reload", "RELOAD");
        m.put("gui.config.save", "SAVE CONFIG");
        m.put("gui.config.saved", "Configuration saved!");
        m.put("gui.config.currency", "Currency");
        m.put("gui.config.limits", "Balance Limits");
        m.put("gui.config.display", "Display");
        m.put("gui.config.language", "Language");
        m.put("gui.config.reset_defaults", "RESET DEFAULTS");
        m.put("gui.config.yes", "Yes");
        m.put("gui.config.no", "No");
        m.put("gui.config.symbol_label", "Symbol");
        m.put("gui.config.name_label", "Name");
        m.put("gui.config.starting_label", "Starting Balance");
        m.put("gui.config.max_label", "Max Balance");
        m.put("gui.config.decimals_label", "Decimals");
        m.put("gui.config.fee_label", "Transfer Fee %");
        m.put("gui.config.hud_label", "Show HUD");
        m.put("gui.config.server_lang", "Server Language");
        m.put("gui.config.per_player", "Per-Player Language");
        m.put("gui.config.reset_done", "Config reset to defaults!");

        // === TOP TAB ===
        m.put("gui.top.no_data", "No data available");

        // === LOG TAB ===
        m.put("gui.log.filter_label", "Filter");
        m.put("gui.log.loading", "Loading...");
        m.put("gui.log.no_transactions", "No transactions");
        m.put("gui.log.showing", "Showing {start}-{end} of {total} (Page {page}/{pages})");
        m.put("gui.log.no_recorded", "No transactions recorded yet");
        m.put("gui.log.no_matches", "No matches for '{filter}'");
        m.put("gui.log.requires_h2", "Transaction log requires H2 storage provider.");

        // === ADMIN ACTIONS ===
        m.put("admin.give", "Gave {amount} to {player}");
        m.put("admin.take", "Took {amount} from {player}");
        m.put("admin.set", "Set {player}'s balance to {amount}");
        m.put("admin.reset", "Reset {player}'s balance");
        m.put("admin.save", "Economy data saved!");
        m.put("admin.reload", "Configuration reloaded!");
        m.put("admin.no_data", "No player data found");
        m.put("admin.insufficient_funds", "Failed - insufficient funds");
        m.put("admin.select_player", "Select a player first");
        m.put("admin.invalid_amount", "Invalid amount (max: {max})");

        // === ERRORS ===
        m.put("error.invalid_amount", "Enter a valid amount");
        m.put("error.max_exceeded", "Amount exceeds maximum balance");
        m.put("error.select_player", "Select a player first");
        m.put("error.no_permission", "You don't have permission to do this");

        // === HUD ===
        m.put("hud.prefix", "Balance");
        m.put("hud.disabled", "Balance HUD disabled due to UI error.");

        return m;
    }

    /**
     * Load messages from messages.json file.
     */
    public static void load() {
        File file = MESSAGES_PATH.toFile();

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            messages = new LinkedHashMap<>(DEFAULTS);
            save();
            LOGGER.log(Level.INFO, "[ArefyEconomy] Created default messages.json");
            return;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> loaded = GSON.fromJson(reader, type);

            if (loaded != null) {
                messages = new LinkedHashMap<>(DEFAULTS);
                messages.putAll(loaded);

                if (loaded.size() < DEFAULTS.size()) {
                    save();
                }

                LOGGER.log(Level.INFO, "[ArefyEconomy] Loaded " + messages.size() + " messages");
            } else {
                messages = new LinkedHashMap<>(DEFAULTS);
                save();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[ArefyEconomy] Error loading messages.json: " + e.getMessage());
            messages = new LinkedHashMap<>(DEFAULTS);
        }
    }

    /**
     * Save current messages to messages.json file.
     */
    public static void save() {
        File file = MESSAGES_PATH.toFile();
        file.getParentFile().mkdirs();

        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(messages, writer);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "[ArefyEconomy] Error saving messages.json: " + e.getMessage());
        }
    }

    /**
     * Get a message by key.
     */
    public static String get(String key) {
        return messages.getOrDefault(key, DEFAULTS.getOrDefault(key, key));
    }

    /**
     * Get a message with placeholder replacement.
     */
    public static String get(String key, String... placeholders) {
        String message = get(key);

        if (placeholders.length % 2 != 0) {
            return message;
        }

        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholder = placeholders[i];
            String value = placeholders[i + 1];
            message = message.replace("{" + placeholder + "}", value);
        }

        return message;
    }

    /**
     * Get a chat message with the prefix.
     */
    public static String chat(String key) {
        return get("chat.prefix") + " " + get(key);
    }

    /**
     * Get a chat message with the prefix and placeholders.
     */
    public static String chat(String key, String... placeholders) {
        return get("chat.prefix") + " " + get(key, placeholders);
    }

    public static void reload() {
        load();
        LOGGER.log(Level.INFO, "[ArefyEconomy] Messages reloaded!");
    }
}
