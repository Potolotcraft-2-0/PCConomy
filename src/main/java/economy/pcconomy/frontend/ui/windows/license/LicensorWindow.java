package economy.pcconomy.frontend.ui.windows.license;

import economy.pcconomy.backend.cash.CashManager;
import economy.pcconomy.backend.license.LicenseManager;
import economy.pcconomy.backend.license.objects.LicenseType;
import economy.pcconomy.frontend.ui.objects.Panel;
import economy.pcconomy.frontend.ui.objects.interactive.Button;

import economy.pcconomy.frontend.ui.windows.Window;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;


public class LicensorWindow extends Window {
    public static final economy.pcconomy.frontend.ui.objects.Panel Panel = new Panel(Arrays.asList(
            new Button(0, 19, "Лицензия на создание торговой зоны", LicenseManager.marketLicensePrice + CashManager.currencySigh, 
                (event) -> LicenseManager.giveLicenseToPlayer((Player)event.getWhoClicked(), LicenseType.Market, LicenseManager.marketLicensePrice)),

            new Button(2, 21, "Лицензия на торговую деятельность", LicenseManager.tradeLicensePrice + CashManager.currencySigh,
                (event) -> LicenseManager.giveLicenseToPlayer((Player)event.getWhoClicked(), LicenseType.Trade, LicenseManager.tradeLicensePrice)),

            new Button(5, 24, "Лицензия на кредитную деятельность", LicenseManager.loanLicensePrice + CashManager.currencySigh,
                (event) -> LicenseManager.giveLicenseToPlayer((Player)event.getWhoClicked(), LicenseType.Loan, LicenseManager.loanLicensePrice)),

            new Button(7, 26, "Лицензия на доступ к кредитной истории", LicenseManager.loanHistoryLicensePrice + CashManager.currencySigh,
                (event) -> LicenseManager.giveLicenseToPlayer((Player)event.getWhoClicked(), LicenseType.LoanHistory, LicenseManager.loanHistoryLicensePrice))

    ), "Panel");

    public Inventory generateWindow(Player player) {
        return Panel.placeComponents(Bukkit.createInventory(player, 27, Component.text("Мир-Лицензии")));
    }
}
