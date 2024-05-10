package economy.pcconomy.backend.npc.traits;

import com.palmergames.bukkit.towny.TownyAPI;
import economy.pcconomy.backend.economy.town.NpcTown;
import economy.pcconomy.backend.economy.town.manager.TownManager;
import economy.pcconomy.frontend.npcTrade.NPCTraderWindow;
import lombok.experimental.ExtensionMethod;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.event.EventHandler;


@TraitName("NPCTrader")
@ExtensionMethod({TownManager.class})
public class NpcTrader extends Trait {
    public NpcTrader() {
        super("NPCTrader");
    }

    @EventHandler
    public void onClick(NPCRightClickEvent event) {
        var player = event.getClicker();
        if (!event.getNPC().equals(this.getNPC())) return;
        
        ((NpcTown)TownyAPI.getInstance().getTownUUID(this.getNPC().getStoredLocation()).getTown()).generateLocalPrices();
        player.openInventory(NPCTraderWindow.generateWindow(player, this.getNPC()));
    }
}
