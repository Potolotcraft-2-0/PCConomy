package economy.pcconomy.frontend.ui.windows.license;

import economy.pcconomy.backend.license.License;
import economy.pcconomy.backend.license.objects.LicenseType;
import economy.pcconomy.frontend.ui.windows.Window;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class LicensorListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        var player = (Player) event.getWhoClicked();
        var option = event.getSlot();

        if (Window.isThisWindow(event, player, "Лицензии")) {
            switch (LicensorWindow.Panel.click(option).getName()) {
                case "Лицензия на создание торговой зоны" ->
                        License.getLicense(player, LicenseType.Market, License.marketLicensePrice);
                case "Лицензия на торговую деятельность" ->
                        License.getLicense(player, LicenseType.Trade, License.tradeLicensePrice);
                case "Лицензия на кредитную деятельность" ->
                        License.getLicense(player, LicenseType.Loan, License.loanLicensePrice);
                case "Лицензия на доступ к кредитной истории" ->
                        License.getLicense(player, LicenseType.LoanHistory, License.loanHistoryLicensePrice);
            }

            event.setCancelled(true);
        }
    }
}
