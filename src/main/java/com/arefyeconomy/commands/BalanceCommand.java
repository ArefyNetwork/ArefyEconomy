package com.arefyeconomy.commands;

import com.arefyeconomy.Main;
import com.arefyeconomy.economy.PlayerBalance;
import com.arefyeconomy.locale.Messages;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;

/**
 * Balance command - shows player's current balance
 */
public class BalanceCommand extends AbstractAsyncCommand {
    
    public BalanceCommand() {
        super("bal", "Check your balance");
        this.addAliases("balance", "money");
        this.setPermissionGroup(GameMode.Adventure);
    }
    
    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        if (commandContext.sender() instanceof Player player) {
            Ref<EntityStore> ref = player.getReference();
            if (ref != null && ref.isValid()) {
                Store<EntityStore> store = ref.getStore();
                
                return CompletableFuture.runAsync(() -> {
                    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                    if (playerRef == null) {
                        player.sendMessage(Message.raw(Messages.get("chat.player_data_error")).color(Color.RED));
                        return;
                    }

                    Main.getInstance().getEconomyManager().ensureAccount(playerRef.getUuid());
                    PlayerBalance balance = Main.getInstance().getEconomyManager().getPlayerBalance(playerRef.getUuid());

                    if (balance == null) {
                        player.sendMessage(Message.raw(Messages.get("chat.error_load_balance")).color(Color.RED));
                        return;
                    }

                    String formattedBalance = Main.CONFIG.get().format(balance.getBalance());

                    player.sendMessage(Message.raw(Messages.get("balance.header")).color(new Color(255, 215, 0)));
                    player.sendMessage(Message.join(
                        Message.raw("  ").color(Color.GRAY),
                        Message.raw(formattedBalance).color(new Color(50, 205, 50)).bold(true)
                    ));
                    
                }, player.getWorld());
            }
        }
        return CompletableFuture.completedFuture(null);
    }
}
