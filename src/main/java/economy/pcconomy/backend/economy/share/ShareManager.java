package economy.pcconomy.backend.economy.share;

import com.google.gson.GsonBuilder;

import economy.pcconomy.PcConomy;
import economy.pcconomy.backend.cash.CashManager;
import economy.pcconomy.backend.economy.share.objects.Share;
import economy.pcconomy.backend.economy.share.objects.ShareType;
import economy.pcconomy.backend.scripts.BalanceManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ShareManager {
    public Map<UUID, List<Share>> Shares;

    /**
     * Expose shares
     * @param town Town who expose shares
     * @param price Price of share
     * @param count Count of shares
     * @param size Size of equality by one share
     * @param shareType Share type
     */
    public void exposeShares(UUID town, double price, int count, double size, ShareType shareType) {
        var shares = new ArrayList<Share>();
        for (var i = 0; i < count; i++)
            shares.add(new Share(town, shareType, null, price, size / (double) count));

        setTownShares(town, shares);
    }

    /**
     * Take off shares from market (Town will need to buy all shares from players by average price)
     * @param town Town who take off shares
     */
    public void takeOffShares(UUID town) {
        var capitalization = 0;
        var averagePrice = getMedianSharePrice(town);

        for (var share : getTownShares(town))
            if (share.Owner != null)
                capitalization++;

        capitalization *= averagePrice;

        if (capitalization > PcConomy.GlobalTownManager.getTown(town).getBudget()) return;

        for (var share : getTownShares(town))
            if (share.Owner != null && Bukkit.getPlayer(share.Owner) != null) {
                PcConomy.GlobalTownManager.getTown(town).changeBudget(-averagePrice);

                new BalanceManager().giveMoney(averagePrice - averagePrice * PcConomy.GlobalBank.VAT,
                        Objects.requireNonNull(Bukkit.getPlayer(share.Owner)));
                PcConomy.GlobalBank.BankBudget += averagePrice * PcConomy.GlobalBank.VAT;
            }

        Shares.remove(town);
    }

    /**
     * Get shares of town
     * @param town Town
     * @return Shares
     */
    public List<Share> getTownShares(UUID town) {
        return Shares.get(town);
    }

    /**
     * Set shares of town
     * @param town Town
     * @param shares Shares
     */
    public void setTownShares(UUID town, List<Share> shares) {
        Shares.put(town, shares);
    }

    /**
     * Player buy share
     * @param town Town that share are buying
     * @param buyer Player who buy share
     */
    public void buyShare(UUID town, Player buyer) {
        for (var share : Shares.get(town))
            if (share.Owner == null) {
                var price = share.Price;
                if (CashManager.amountOfCashInInventory(buyer) >= price + price * PcConomy.GlobalBank.VAT) {
                    CashManager.takeCashFromInventory(price + price * PcConomy.GlobalBank.VAT, buyer);

                    PcConomy.GlobalBank.BankBudget += price * PcConomy.GlobalBank.VAT;
                    PcConomy.GlobalTownManager.getTown(town).changeBudget(price);

                    share.Owner = buyer.getUniqueId();
                }
            }
    }

    /**
     * Player sell share
     * @param town Town that share are selling
     * @param seller  Player who buy share
     */
    public void sellShare(UUID town, Player seller) {
        var currentTown = PcConomy.GlobalTownManager.getTown(town);
        if (currentTown == null) return;

        for (var share : Shares.get(town))
            if (share.Owner == seller.getUniqueId()) {
                var price = getMedianSharePrice(town);

                if (currentTown.getBudget() >= price) {
                    CashManager.giveCashToPlayer(price - price * PcConomy.GlobalBank.VAT, seller);

                    PcConomy.GlobalBank.BankBudget += price * PcConomy.GlobalBank.VAT;
                    PcConomy.GlobalTownManager.getTown(town).changeBudget(-price);

                    share.Owner = null;
                }
            }
    }

    /**
     * Get average price of share
     * @param town Town
     * @return Average price
     */
    public double getMedianSharePrice(UUID town) {
        var price = 0;
        var shares = Shares.get(town);
        for (var share : shares)
            if (share.Owner == null)
                price += share.Price;

        return price / (double)shares.size();
    }

    /**
     * Daily pay
     */
    public void dailyPaying() {
        var towns = Shares.keySet();
        for (var town : towns)
            payDividends(town);
    }

    /**
     * Town pay for share owners
     * @param town Town that pay
     */
    public void payDividends(UUID town) {
        var townObject = PcConomy.GlobalTownManager.getTown(town);
        for (var shares : Shares.get(town)) {
            if (shares.ShareType == ShareType.Equity) break;
            if (townObject.quarterlyEarnings < 0) break;
            if (shares.Owner == null) continue;

            var pay = townObject.quarterlyEarnings * shares.Equality;
            var player = Bukkit.getPlayer(shares.Owner);
            if (player == null) continue;

            townObject.changeBudget(-pay);

            PcConomy.GlobalBank.BankBudget += pay * PcConomy.GlobalBank.VAT;
            new BalanceManager().giveMoney(pay - pay * PcConomy.GlobalBank.VAT, player);
        }

        townObject.quarterlyEarnings = 0;
    }

    /**
     * Saves license
     * @param fileName File name
     * @throws IOException If something goes wrong
     */
    public void saveShares(String fileName) throws IOException {
        FileWriter writer = new FileWriter(fileName + ".json", false);
        new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
                .toJson(this, writer);
        writer.close();
    }
}