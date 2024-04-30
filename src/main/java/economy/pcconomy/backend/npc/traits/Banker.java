package economy.pcconomy.backend.npc.traits;

import economy.pcconomy.frontend.windows.Window;
import economy.pcconomy.frontend.windows.bank.BankerWindow;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.event.EventHandler;


@TraitName("Banker")
public class Banker extends Trait {
    public Banker() {
        super("Banker");
    }

    @EventHandler
    public void onClick(NPCRightClickEvent event) {
        if (!event.getNPC().equals(this.getNPC())) return;
        Window.openWindow(event.getClicker(), new BankerWindow());
    }
}
