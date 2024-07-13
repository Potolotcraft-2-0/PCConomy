package economy.pcconomy.frontend;

import com.palmergames.bukkit.towny.TownyAPI;
import economy.pcconomy.PcConomy;
import economy.pcconomy.backend.cash.Cash;
import economy.pcconomy.backend.economy.bank.Bank;
import economy.pcconomy.backend.npc.NpcManager;
import economy.pcconomy.backend.npc.traits.Trader;

import lombok.experimental.ExtensionMethod;
import net.citizensnpcs.api.CitizensAPI;
import net.kyori.adventure.text.Component;

import net.potolotcraft.gorodki.GorodkiUniverse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import org.j1sk1ss.itemmanager.manager.Manager;
import org.j1sk1ss.menuframework.objects.MenuSizes;
import org.j1sk1ss.menuframework.objects.MenuWindow;
import org.j1sk1ss.menuframework.objects.interactive.components.Button;
import org.j1sk1ss.menuframework.objects.interactive.components.ClickArea;
import org.j1sk1ss.menuframework.objects.interactive.components.Icon;
import org.j1sk1ss.menuframework.objects.interactive.components.Panel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static economy.pcconomy.frontend.TraderWindow.getTraderFromTitle;


@ExtensionMethod({Cash.class, Manager.class})
public class MayorManagerWindow implements Listener {
    @SuppressWarnings("deprecation")
    public static MenuWindow TraderManager = new MenuWindow(Arrays.asList(
        new Panel(List.of(
            new ClickArea(0, 26, 
                (event) -> {
                    var player = (Player)event.getWhoClicked();
                    var inventory = event.getInventory();

                    var traderId = Integer.parseInt(Objects.requireNonNull(inventory.getItem(event.getSlot())).getName());
                    MayorManagerWindow.generateTradeControls(player, traderId);
                }),

            new Button(27, 35, "Купить торговца", "Купить нового торговца",
                (event) -> new Trader().Buy((Player) event.getWhoClicked()))
        ), "Город-Торговцы", MenuSizes.FourLines, "\u10D3"),

        new Panel(Arrays.asList(
            new Button(0, 20, "Уволить торговца", "Торговец будет уволен",
                (event) -> {
                    var title  = event.getView().getTitle();
                    var trader = getTraderFromTitle(title);
                    if (trader == null) return;
                    if (trader.IsRanted) return;

                    trader.destroy();
                }, Material.GOLD_INGOT, 7000),

            new Button(3, 23, "Переместить торговца", "Торговец будет перемещён в место вашего клика",
                (event) -> {
                    var player = (Player)event.getWhoClicked();
                    var title  = event.getView().getTitle();
                    var trader = getTraderFromTitle(title);
                    if (trader == null) return;

                    var key = new NamespacedKey(PcConomy.getPlugin(PcConomy.class), "trader-move");
                    if (!player.getPersistentDataContainer().has(key, PersistentDataType.INTEGER))
                        player.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, trader.getNPC().getId());
                }, Material.GOLD_INGOT, 7000),

            new Button(6, 26, "Улучшить торговца", "Торговец будет улучшен (+9 слотов)",
                (event) -> {
                    var player = (Player)event.getWhoClicked();
                    var title  = event.getView().getTitle();
                    var trader = getTraderFromTitle(title);
                    if (trader == null) return;
                    if (trader.Level >= 6) return;

                    var inventoryAmount = player.amountOfCashInInventory(false);
                    var price = NpcManager.traderCost * trader.Level;
                    if (Bank.getValueWithVat(price) > inventoryAmount) return;

                    trader.Level = Math.min(trader.Level + 1, 6);
                    player.takeCashFromPlayer(PcConomy.GlobalBank.getBank().addVAT(price), false);
                }, Material.GOLD_INGOT, 7000)
        ), "Город-Торговцы-Управление", MenuSizes.ThreeLines, "\u10D4")
    ));

    public static void generateWindow(Player player) {
        var components = new ArrayList<org.j1sk1ss.menuframework.objects.interactive.Component>();
        var town = GorodkiUniverse.getInstance().getGorod(TownyAPI.getInstance().getTown(player));
        for (var i = 0; i < Math.min(27, town.getTraders().size()); i++) {
            var trader = CitizensAPI.getNPCRegistry().getById(town.getTraders().get(i)).getOrAddTrait(Trader.class);
            components.add(new Icon(i, town.getTraders().get(i) + "",
                "Ranted: " + trader.IsRanted + "\nMargin: " + trader.Margin + "\nRant price: " + trader.Cost, Material.GOLD_INGOT, 7001)); // TODO: Icons for traders
        }

        TraderManager.getPanel("Город-Торговцы").getViewWith(player, components);
    }

    public static void generateTradeControls(Player player, int traderId) {
        TraderManager.getPanel("Город-Торговцы-Управление").getView(player, "Город-Торговцы-Управление " + traderId);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        var player = (Player)event.getPlayer();
        var container = player.getPersistentDataContainer();
        var block = event.getClickedBlock();
        var key = new NamespacedKey(PcConomy.getPlugin(PcConomy.class), "trader-move");

        if (block == null) return;
        if (container.has(key, PersistentDataType.INTEGER)) {
            var id = container.get(key, PersistentDataType.INTEGER);
            if (id == null) return;

            var trader = NpcManager.getNPC(id);
            trader.teleport(block.getLocation().add(0, 1, 0), PlayerTeleportEvent.TeleportCause.ENDER_PEARL);

            container.remove(key);
        }
    }
}
