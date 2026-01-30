package com.arefyeconomy.commands;

import com.arefyeconomy.Main;
import com.arefyeconomy.economy.EconomyManager;
import com.arefyeconomy.gui.PayGui;
import com.arefyeconomy.locale.Messages;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;

/**
 * Pay command - Transfer money to another player.
 * 
 * Usage: 
 * - /pay              → Opens PayGui (interactive interface)
 * - /pay <player> <amount> → Direct transfer
 */
public class PayCommand extends AbstractAsyncCommand {
    
    private final OptionalArg<PlayerRef> playerArg;
    private final OptionalArg<Double> amountArg;
    
    public PayCommand() {
        super("pay", "Send money to another player");
        this.setPermissionGroup(GameMode.Adventure);
        
        // Use OptionalArg to allow GUI mode when no args provided
        this.playerArg = this.withOptionalArg("player", "The player to send money to", ArgTypes.PLAYER_REF);
        this.amountArg = this.withOptionalArg("amount", "The amount to send", ArgTypes.DOUBLE);
    }
    
    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
        CommandSender sender = ctx.sender();
        
        if (!(sender instanceof Player player)) {
            ctx.sendMessage(Message.raw(Messages.get("chat.players_only")).color(Color.RED));
            return CompletableFuture.completedFuture(null);
        }

        var senderEntity = player.getReference();
        if (senderEntity == null || !senderEntity.isValid()) {
            ctx.sendMessage(Message.raw(Messages.get("chat.player_data_error")).color(Color.RED));
            return CompletableFuture.completedFuture(null);
        }

        var senderStore = senderEntity.getStore();
        var world = senderStore.getExternalData().getWorld();
        if (world == null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        world.execute(() -> {
            PlayerRef senderRef = senderStore.getComponent(senderEntity, PlayerRef.getComponentType());
            if (senderRef == null) {
                ctx.sendMessage(Message.raw(Messages.get("chat.player_ref_error")).color(Color.RED));
                future.complete(null);
                return;
            }

            PlayerRef targetRef = ctx.get(playerArg);
            Double amount = ctx.get(amountArg);

            if (targetRef == null && amount == null) {
                player.getPageManager().openCustomPage(senderEntity, senderStore, new PayGui(senderRef));
                future.complete(null);
                return;
            }

            if (targetRef == null) {
                ctx.sendMessage(Message.raw(Messages.get("pay.usage")).color(Color.GRAY));
                future.complete(null);
                return;
            }

            if (amount == null || amount <= 0) {
                ctx.sendMessage(Message.raw(Messages.get("pay.amount_positive")).color(Color.RED));
                future.complete(null);
                return;
            }

            double minTx = Main.CONFIG.get().getMinimumTransaction();
            if (amount < minTx) {
                ctx.sendMessage(Message.raw(Messages.get("pay.minimum_transaction", "amount", Main.CONFIG.get().format(minTx))).color(Color.RED));
                future.complete(null);
                return;
            }

            EconomyManager.TransferResult result = Main.getInstance().getEconomyManager()
                .transfer(senderRef.getUuid(), targetRef.getUuid(), amount, "Player payment");

            switch (result) {
                case SUCCESS -> {
                    double fee = amount * Main.CONFIG.get().getTransferFee();
                    String feeText = fee > 0 ? Messages.get("pay.success_fee", "fee", Main.CONFIG.get().format(fee)) : "";
                    player.sendMessage(Message.join(
                        Message.raw(Messages.get("pay.success", "amount", Main.CONFIG.get().format(amount))).color(Color.GREEN),
                        Message.raw(feeText).color(Color.GRAY)
                    ));
                }
                case INSUFFICIENT_FUNDS -> {
                    double balance = Main.getInstance().getEconomyManager().getBalance(senderRef.getUuid());
                    player.sendMessage(Message.raw(Messages.get("pay.insufficient_funds", "balance", Main.CONFIG.get().format(balance))).color(Color.RED));
                }
                case SELF_TRANSFER -> {
                    player.sendMessage(Message.raw(Messages.get("pay.self_transfer")).color(Color.RED));
                }
                case INVALID_AMOUNT -> {
                    player.sendMessage(Message.raw(Messages.get("pay.invalid_amount")).color(Color.RED));
                }
                case RECIPIENT_MAX_BALANCE -> {
                    player.sendMessage(Message.raw(Messages.get("pay.recipient_max_balance")).color(Color.RED));
                }
            }

            future.complete(null);
        });

        return future;
    }
}
