package economy.pcconomy.frontend.ui.windows.bank;

import economy.pcconomy.PcConomy;
import economy.pcconomy.backend.cash.CashManager;
import economy.pcconomy.frontend.ui.windows.IWindowListener;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.j1sk1ss.itemmanager.manager.Manager;

import lombok.experimental.ExtensionMethod;


@ExtensionMethod({Manager.class, CashManager.class})
public class BankerListener implements IWindowListener {
    public void onClick(InventoryClickEvent event) {
        var player = (Player) event.getWhoClicked();
        var option = event.getCurrentItem();
        if (option == null) return;

        if (option.getLoreLines().size() < 2) return;
        var amount = option.getPriceFromLore(1);

        if (amount > 0) PcConomy.GlobalBank.giveCashToPlayer(amount, player);
        else PcConomy.GlobalBank.takeCashFromPlayer(Math.abs(amount), player);

        BankerWindow.regenerateWindow(player, event.getInventory());
    }
}
