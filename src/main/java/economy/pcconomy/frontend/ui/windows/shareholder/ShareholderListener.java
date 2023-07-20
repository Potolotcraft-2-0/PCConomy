package economy.pcconomy.frontend.ui.windows.shareholder;

import com.palmergames.bukkit.towny.TownyAPI;

import economy.pcconomy.PcConomy;
import economy.pcconomy.backend.cash.CashManager;
import economy.pcconomy.backend.economy.share.objects.ShareType;
import economy.pcconomy.backend.scripts.items.ItemManager;
import economy.pcconomy.frontend.ui.objects.interactive.Slider;
import economy.pcconomy.frontend.ui.windows.Window;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;
import java.util.UUID;

public class ShareholderListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        var player = (Player) event.getWhoClicked();
        var option = event.getSlot();

        if (Window.isThisWindow(event, player, "Акции-Меню")) {
            switch (ShareholderWindow.ShareHolderMenu.getPanel("Акции-Меню").click(option).getName()) {
                case "Покупка/продажа акций" -> player.openInventory(ShareholderWindow.sharesWindow(player, 0));
                case "Выставление акций" -> {
                    var town = TownyAPI.getInstance().getTown(player);
                    if (town != null)
                        if (PcConomy.GlobalShareManager.ActionsList.contains(town.getUUID())) {
                            player.sendMessage("Ваш город уже работал с акциями сегодня");
                            return;
                        }

                    if (player.equals(Objects.requireNonNull(town).getMayor().getPlayer()))
                        player.openInventory(ShareholderWindow.townSharesWindow(player, town.getUUID()));
                }
            }

            event.setCancelled(true);
        }

        if (Window.isThisWindow(event, player, "Акции-Список")) {
            var item = event.getCurrentItem();
            if (item == null) return;

            var townId = UUID.fromString(ItemManager.getLore(item).get(3).split(" ")[1]);
            player.openInventory(ShareholderWindow.acceptWindow(player, townId));

            event.setCancelled(true);
        }

        if (Window.isThisWindow(event, player, "Акции-Города")) {
            var town = TownyAPI.getInstance().getTown(event.getView().getTitle().split(" ")[1]);
            if (town == null) return;

            var share = PcConomy.GlobalShareManager.getEmptyTownShare(town.getUUID());
            if (share == null) return;

            event.setCancelled(true);

            switch (ShareholderWindow.ShareHolderMenu.getPanel("Акции-Города").click(option).getName()) {
                case "Продать одну акцию" -> {
                    if (share.Price > PcConomy.GlobalTownManager.getTown(town.getUUID()).getBudget()) return;
                    PcConomy.GlobalShareManager.sellShare(town.getUUID(), player);
                }
                case "Купить одну акцию" -> {
                    if (share.Price + share.Price * PcConomy.GlobalBank.VAT > CashManager.amountOfCashInInventory(player, false)) return;
                    PcConomy.GlobalShareManager.buyShare(town.getUUID(), player);
                }
            }
        }

        if (Window.isThisWindow(event, player, "Акции-Выставление")) {
            var townSharesPanel = ShareholderWindow.ShareHolderMenu.getPanel("Акции-Выставление");
            if (townSharesPanel.click(option).getName().contains("Slider")) {
                var slider = new Slider((Slider)townSharesPanel.click(option));

                slider.setChose(option);
                slider.place(event.getInventory());
            }

            switch (townSharesPanel.click(option).getName()) {
                case "Выставить на продажу" -> {
                    var town = TownyAPI.getInstance().getTown(event.getView().getTitle().split(" ")[1]);
                    if (town == null) return;

                    var countSlider = new Slider(townSharesPanel.getSliders("SliderCount"));
                    var percentSlider = new Slider(townSharesPanel.getSliders("SliderPercent"));
                    var costSlider = new Slider(townSharesPanel.getSliders("SliderCost"));
                    var typeSlider = new Slider(townSharesPanel.getSliders("SliderType"));

                    PcConomy.GlobalShareManager.exposeShares(
                            town.getUUID(),
                            Double.parseDouble(ItemManager.getName(costSlider.getChose()).replace(CashManager.currencySigh, "")),
                            Integer.parseInt(ItemManager.getName(countSlider.getChose()).replace("шт.", "")),
                            Double.parseDouble(ItemManager.getName(percentSlider.getChose()).replace("%", "")),
                            (ItemManager.getName(typeSlider.getChose()).equals("Дивиденты") ? ShareType.Dividends : ShareType.Equity));

                }
                case "Снять с продажи" -> {
                    var town = TownyAPI.getInstance().getTown(event.getView().getTitle().split(" ")[1]);
                    if (town == null) return;

                    PcConomy.GlobalShareManager.takeOffShares(town.getUUID());
                }
            }

            event.setCancelled(true);
        }
    }
}