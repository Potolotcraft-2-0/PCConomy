package economy.pcconomy.frontend.ui.windows.wallet;

import economy.pcconomy.backend.cash.CashManager;
import economy.pcconomy.backend.cash.items.Wallet;
import economy.pcconomy.backend.scripts.items.ItemManager;
import economy.pcconomy.frontend.ui.windows.IWindowListener;
import economy.pcconomy.frontend.ui.windows.Window;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import lombok.experimental.ExtensionMethod;


@ExtensionMethod({ItemManager.class})
public class WalletListener implements Listener, IWindowListener {
    @EventHandler
    public void onWalletUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_AIR) return;

        var player = event.getPlayer();
        var wallet = Wallet.isWallet(player.getInventory().getItemInMainHand()) ?
                new Wallet(player.getInventory().getItemInMainHand()) :
                null;

        if (wallet != null) {
            switch (event.getAction()) {
                case LEFT_CLICK_AIR -> player.openInventory(WalletWindow.putWindow(player, wallet));
                case RIGHT_CLICK_AIR -> player.openInventory(WalletWindow.withdrawWindow(player, wallet));
                default -> throw new IllegalArgumentException("Unexpected value: " + event.getAction());
            }

            event.setCancelled(true);
        }
    }

    public void onClick(InventoryClickEvent event) {
        var player = (Player) event.getWhoClicked();
        var currentItem = player.getInventory().getItemInMainHand();
        if (currentItem.getAmount() > 1 && Wallet.isWallet(currentItem)) {
            player.sendMessage("Выберите один кошелёк");
            event.setCancelled(true);
            return;
        }

        var wallet = Wallet.isWallet(currentItem) ? new Wallet(player.getInventory().getItemInMainHand()) : null;
        if (wallet == null) return;

        player.getInventory().setItemInMainHand(null);
        if (Window.isThisWindow(event, player, "Кошелёк")) {
            var option = event.getCurrentItem();
            if (option == null) return;

            if (option.getLoreLines().size() < 2) return;
            var amount = option.getPriceFromLore(1);

            if (amount > 0) {
                CashManager.giveCashToPlayer(Math.abs(amount), player, true);
                wallet.changeCashInWallet(-amount);
            }
            else {
                CashManager.takeCashFromPlayer(Math.abs(amount), player, true);
                wallet.changeCashInWallet(Math.abs(amount));
            }

            player.closeInventory();
            wallet.giveWallet(player);
            event.setCancelled(true);
        }
    }
}