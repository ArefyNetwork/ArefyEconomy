package com.arefyeconomy.gui;

import com.arefyeconomy.Main;
import com.arefyeconomy.economy.PlayerBalance;
import com.arefyeconomy.locale.Messages;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Simple UI for viewing top balances.
 * Public command /baltop - no admin features, just the leaderboard.
 */
public class BaltopGui extends InteractiveCustomUIPage<BaltopGui.BaltopData> {

    private final PlayerRef playerRef;

    public BaltopGui(@NonNullDecl PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, BaltopData.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder cmd,
                      @NonNullDecl UIEventBuilder events, @NonNullDecl Store<EntityStore> store) {
        cmd.append("Pages/ArefyEconomy_BaltopPage.ui");

        // Close button binding
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton",
            EventData.of("Action", "Close"), false);

        // Set title
        cmd.set("#Title.Text", Messages.get("gui.baltop.title"));

        // Build the top list
        buildTopList(cmd);
    }

    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store,
                                @NonNullDecl BaltopData data) {
        super.handleDataEvent(ref, store, data);

        if (data.action != null && data.action.equals("Close")) {
            this.close();
        }
    }

    private void buildTopList(@NonNullDecl UICommandBuilder cmd) {
        cmd.clear("#TopList");

        var allBalances = Main.getInstance().getEconomyManager().getAllBalances();

        // Sort by balance descending, take top 10
        List<Map.Entry<UUID, PlayerBalance>> top10 = allBalances.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue().getBalance(), a.getValue().getBalance()))
            .limit(10)
            .collect(Collectors.toList());

        int rank = 1;
        for (var entry : top10) {
            UUID uuid = entry.getKey();
            PlayerBalance balance = entry.getValue();
            String playerName = getPlayerName(uuid);

            cmd.append("#TopList", "Pages/ArefyEconomy_AdminTopEntry.ui");
            cmd.set("#TopList[" + (rank - 1) + "] #Rank.Text", "#" + rank);
            cmd.set("#TopList[" + (rank - 1) + "] #PlayerName.Text", playerName);
            cmd.set("#TopList[" + (rank - 1) + "] #PlayerBalance.Text", Main.CONFIG.get().format(balance.getBalance()));

            rank++;
        }

        if (top10.isEmpty()) {
            cmd.appendInline("#TopList", "Label { Text: \"" + Messages.get("gui.top.no_data") + "\"; Style: (FontSize: 14, TextColor: #888888); Padding: (Top: 20); }");
        }
    }

    private String getPlayerName(UUID uuid) {
        // Try to get from online players first
        PlayerRef onlinePlayer = Universe.get().getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getUsername();
        }

        // Try H2 storage for offline players
        var h2Storage = Main.getInstance().getEconomyManager().getH2Storage();
        if (h2Storage != null) {
            String name = h2Storage.getPlayerName(uuid);
            if (name != null) {
                return name;
            }
        }

        return uuid.toString().substring(0, 8) + "...";
    }

    // ========== Data Codec ==========

    public static class BaltopData {
        private static final String KEY_ACTION = "Action";

        public static final BuilderCodec<BaltopData> CODEC = BuilderCodec.<BaltopData>builder(BaltopData.class, BaltopData::new)
            .append(new KeyedCodec<>(KEY_ACTION, Codec.STRING), (d, v, e) -> d.action = v, (d, e) -> d.action).add()
            .build();

        private String action;
    }
}
