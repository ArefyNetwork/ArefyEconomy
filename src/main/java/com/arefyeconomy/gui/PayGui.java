package com.arefyeconomy.gui;

import com.arefyeconomy.Main;
import com.arefyeconomy.economy.EconomyManager;
import com.arefyeconomy.locale.Messages;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.Color;
import java.util.UUID;

/**
 * PayGui - Interactive GUI for sending payments to other players.
 * 
 * Triggered by: /pay (without arguments)
 * 
 * Features:
 * - List of online players with search
 * - Amount input with fee preview
 * - Confirmation before sending
 */
public class PayGui extends InteractiveCustomUIPage<PayGui.PayGuiData> {
    
    // Store playerRef for per-player translations
    private final PlayerRef playerRef;
    
    private String searchQuery = "";
    private String selectedPlayerUuid = null;
    private String amountInput = "";
    
    public PayGui(@NonNullDecl PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, PayGuiData.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder cmd, 
                      @NonNullDecl UIEventBuilder events, @NonNullDecl Store<EntityStore> store) {
        // Load main page layout
        cmd.append("Pages/ArefyEconomy_PayPage.ui");
        
        // Set initial balance
        double balance = Main.getInstance().getEconomyManager().getBalance(playerRef.getUuid());
        cmd.set("#BalanceValue.Text", Main.CONFIG.get().format(balance));
        
        cmd.set("#SearchInput.Value", this.searchQuery);
        cmd.set("#AmountInput.Value", this.amountInput);
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput",
            EventData.of("@SearchQuery", "#SearchInput.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AmountInput",
            EventData.of("@AmountInput", "#AmountInput.Value"), false);
        
        // Setup buttons
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", 
            EventData.of("Action", "Close"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ConfirmButton", 
            EventData.of("Action", "Confirm"), false);
        
        // Build player list
        buildPlayerList(cmd, events, store);
        
        // Update fee preview
        updateFeePreview(cmd);
        
        // Translate UI elements based on server config language
        translateUI(cmd);
    }
    
    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store, 
                                @NonNullDecl PayGuiData data) {
        super.handleDataEvent(ref, store, data);
        
        // Handle search query change
        if (data.searchQuery != null) {
            this.searchQuery = data.searchQuery.trim().toLowerCase();
            refreshUI(ref, store);
            return;
        }
        
        // Handle amount input change
        if (data.amountInput != null) {
            this.amountInput = data.amountInput;
            UICommandBuilder cmd = new UICommandBuilder();
            updateFeePreview(cmd);
            this.sendUpdate(cmd, new UIEventBuilder(), false);
            return;
        }
        
        // Handle player selection
        if (data.selectedPlayer != null) {
            this.selectedPlayerUuid = data.selectedPlayer;
            refreshUI(ref, store);
            return;
        }
        
        // Handle actions
        if (data.action != null) {
            switch (data.action) {
                case "Close" -> {
                    this.close();
                    return;
                }
                case "Confirm" -> {
                    handleConfirmPayment(ref, store);
                    return;
                }
            }
        }
        
        this.sendUpdate();
    }
    
    private void buildPlayerList(@NonNullDecl UICommandBuilder cmd, @NonNullDecl UIEventBuilder events,
                                  @NonNullDecl Store<EntityStore> store) {
        cmd.clear("#PlayerList");
        
        int index = 0;
        for (PlayerRef player : Universe.get().getPlayers()) {
            // Skip self
            if (player.getUuid().equals(playerRef.getUuid())) {
                continue;
            }
            
            String username = player.getUsername();
            
            // Apply search filter
            if (!searchQuery.isEmpty() && !username.toLowerCase().contains(searchQuery)) {
                continue;
            }
            
            String uuid = player.getUuid().toString();
            
            // Append player entry
            cmd.append("#PlayerList", "Pages/ArefyEconomy_PayPlayerEntry.ui");
            cmd.set("#PlayerList[" + index + "] #PlayerName.Text", username);
            
            // Show selection indicator
            boolean isSelected = uuid.equals(selectedPlayerUuid);
            cmd.set("#PlayerList[" + index + "] #SelectedIcon.Visible", isSelected);
            
            // Bind click event
            events.addEventBinding(CustomUIEventBindingType.Activating, "#PlayerList[" + index + "]",
                EventData.of("SelectedPlayer", uuid), false);
            
            index++;
        }
        
        // Show empty message if no players
        if (index == 0) {
            cmd.appendInline("#PlayerList", "Group { LayoutMode: Center; Anchor: (Height: 80); " +
                "Label { Text: \"" + Messages.get("gui.pay.no_players") + "\"; Style: (FontSize: 14, TextColor: #888888, HorizontalAlignment: Center); } }");
        }
    }
    
    private void updateFeePreview(@NonNullDecl UICommandBuilder cmd) {
        double amount = parseAmount(amountInput);
        double feePercent = Main.CONFIG.get().getTransferFee();
        double fee = amount * feePercent;
        double currentBalance = Main.getInstance().getEconomyManager().getBalance(playerRef.getUuid());
        double balanceAfter = currentBalance - amount - fee;
        
        cmd.set("#FeeValue.Text", Main.CONFIG.get().format(fee));
        
        // Balance after: green if positive, red if negative
        if (balanceAfter >= 0) {
            cmd.set("#BalanceAfterValue.Text", Main.CONFIG.get().format(balanceAfter));
        } else {
            cmd.set("#BalanceAfterValue.Text", Messages.get("gui.pay.insufficient"));
        }
    }
    
    private void handleConfirmPayment(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store) {
        // Validate selection
        if (selectedPlayerUuid == null) {
            playerRef.sendMessage(Message.raw(Messages.get("gui.pay.select_player_first")).color(Color.RED));
            return;
        }

        // Validate amount
        double amount = parseAmount(amountInput);
        if (amount <= 0) {
            playerRef.sendMessage(Message.raw(Messages.get("pay.invalid_amount")).color(Color.RED));
            return;
        }

        // Check minimum transaction
        double minTx = Main.CONFIG.get().getMinimumTransaction();
        if (amount < minTx) {
            playerRef.sendMessage(Message.raw(Messages.get("pay.minimum_transaction", "amount", Main.CONFIG.get().format(minTx))).color(Color.RED));
            return;
        }

        // Execute transfer
        UUID targetUuid = UUID.fromString(selectedPlayerUuid);
        EconomyManager.TransferResult result = Main.getInstance().getEconomyManager()
            .transfer(playerRef.getUuid(), targetUuid, amount, "GUI payment");

        switch (result) {
            case SUCCESS -> {
                double fee = amount * Main.CONFIG.get().getTransferFee();
                String feeText = fee > 0 ? Messages.get("pay.success_fee", "fee", Main.CONFIG.get().format(fee)) : "";
                playerRef.sendMessage(Message.raw(Messages.get("pay.success", "amount", Main.CONFIG.get().format(amount)) + feeText).color(Color.GREEN));
                this.close();
            }
            case INSUFFICIENT_FUNDS -> {
                double balance = Main.getInstance().getEconomyManager().getBalance(playerRef.getUuid());
                playerRef.sendMessage(Message.raw(Messages.get("pay.insufficient_funds", "balance", Main.CONFIG.get().format(balance))).color(Color.RED));
            }
            case SELF_TRANSFER -> {
                playerRef.sendMessage(Message.raw(Messages.get("pay.self_transfer")).color(Color.RED));
            }
            case RECIPIENT_MAX_BALANCE -> {
                playerRef.sendMessage(Message.raw(Messages.get("pay.recipient_max_balance")).color(Color.RED));
            }
            default -> {
                playerRef.sendMessage(Message.raw(Messages.get("pay.transfer_failed")).color(Color.RED));
            }
        }
    }
    
    private void refreshUI(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store) {
        UICommandBuilder cmd = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();
        buildPlayerList(cmd, events, store);
        updateFeePreview(cmd);
        this.sendUpdate(cmd, events, false);
    }

    
    private double parseAmount(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(input.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /** Translate all static UI elements using Messages.java */
    private void translateUI(@NonNullDecl UICommandBuilder cmd) {
        cmd.set("#Title.Text", Messages.get("gui.pay.title"));
        cmd.set("#BalanceLabel.Text", Messages.get("gui.pay.your_balance_label"));
        cmd.set("#SearchLabel.Text", Messages.get("gui.pay.search_label"));
        cmd.set("#AmountLabel.Text", Messages.get("gui.pay.amount"));
        cmd.set("#FeeLabel.Text", Messages.get("gui.pay.fee_label"));
        cmd.set("#BalAfterLabel.Text", Messages.get("gui.pay.balance_after"));
        cmd.set("#ConfirmButton.Text", Messages.get("gui.pay.send"));
    }
    
    // ========== Data Codec ==========
    
    public static class PayGuiData {
        private static final String KEY_SEARCH = "@SearchQuery";
        private static final String KEY_AMOUNT = "@AmountInput";
        private static final String KEY_SELECTED = "SelectedPlayer";
        private static final String KEY_ACTION = "Action";
        
        public static final BuilderCodec<PayGuiData> CODEC = BuilderCodec.<PayGuiData>builder(PayGuiData.class, PayGuiData::new)
            .append(new KeyedCodec<>(KEY_SEARCH, Codec.STRING), (d, v, e) -> d.searchQuery = v, (d, e) -> d.searchQuery).add()
            .append(new KeyedCodec<>(KEY_AMOUNT, Codec.STRING), (d, v, e) -> d.amountInput = v, (d, e) -> d.amountInput).add()
            .append(new KeyedCodec<>(KEY_SELECTED, Codec.STRING), (d, v, e) -> d.selectedPlayer = v, (d, e) -> d.selectedPlayer).add()
            .append(new KeyedCodec<>(KEY_ACTION, Codec.STRING), (d, v, e) -> d.action = v, (d, e) -> d.action).add()
            .build();
        
        private String searchQuery;
        private String amountInput;
        private String selectedPlayer;
        private String action;
    }
}
