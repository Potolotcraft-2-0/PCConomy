package economy.pcconomy.scripts;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ItemWorker {
    public static ItemStack SetLore(ItemStack item, String loreLine) { // Устанавливает лор для предмета
        var itemMeta = Objects.requireNonNull(item).getItemMeta();
        var lore = Arrays.stream(loreLine.split("\n")).toList();

        itemMeta.setLore(lore);
        Objects.requireNonNull(item).setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack SetName(ItemStack item, String name) { // Устанавливает имя для предмета
        var itemMeta = Objects.requireNonNull(item).getItemMeta();
        itemMeta.setDisplayName(name);
        Objects.requireNonNull(item).setItemMeta(itemMeta);

        return item;
    }

    public static List<String> GetLore(ItemStack item) {
        return item.getItemMeta().getLore();
    } // Получает лор

    public static String GetName(ItemStack item){
        return item.getItemMeta().getDisplayName();
    } // Получает имя

    public static void giveItems(List<ItemStack> itemStacks, Player player) { // Выдаёт лист предметов игроку
        if (getEmptySlots(player) < itemStacks.size()) return;

        for (var item:
                itemStacks) {
            player.getInventory().addItem(item);
        }
    }

    public static void giveItems(ItemStack itemStack, Player player) { // Выдаёт один предмет игроку
        if (getEmptySlots(player) < 1) return;
        player.getInventory().addItem(itemStack);
    }

    public static void TakeItems(List<ItemStack> itemStacks, Player player) {
        for (var item:
             itemStacks) {
            for (var playerItem:
                 player.getInventory()) {
                if (playerItem.equals(item)) player.getInventory().removeItem(playerItem);
            }
        }
    }

    public static void TakeItems(ItemStack itemStack, Player player) {
        for (var playerItem:
                player.getInventory()) {
            if (playerItem.equals(itemStack)) player.getInventory().removeItem(playerItem);
        }
    }

    public static int getEmptySlots(Player player) { // Получает кол-во пустых слотов у игрока
        var inventory = player.getInventory();
        var cont = inventory.getContents();
        int i = 0;
        for (var item : cont)
            if (item != null && item.getType() != Material.AIR) {
                i++;
            }
        return 36 - i;
    }
}