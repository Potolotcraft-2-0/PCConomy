package economy.pcconomy.backend.economy.credit;

import com.google.gson.GsonBuilder;

import economy.pcconomy.PcConomy;

import economy.pcconomy.backend.db.Loadable;
import org.bukkit.entity.Player;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class BorrowerManager implements Loadable {
    public final List<Borrower> borrowers = new ArrayList<>();

    /**
     * Get borrower object of player
     * @param player Player
     * @return Borrower object
     */
    public static Borrower getBorrowerObject(Player player) {
        for (var borrower : PcConomy.GlobalBorrower.borrowers)
            if (borrower.Borrower.equals(player.getUniqueId())) return borrower;

        return null;
    }

    /**
     * Update or sets new borrower object of player
     * @param borrowerObject New borrower object
     */
    public static void setBorrowerObject(Borrower borrowerObject) {
        for (var borrower = 0; borrower < PcConomy.GlobalBorrower.borrowers.size(); borrower++)
            if (PcConomy.GlobalBorrower.borrowers.get(borrower).Borrower.equals(borrowerObject.Borrower))
                PcConomy.GlobalBorrower.borrowers.set(borrower, borrowerObject);
    }

    @Override
    public void save(String fileName) throws IOException {
        var writer = new FileWriter(fileName + ".json", false);
        new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
                .toJson(this, writer);
        writer.close();
    }

    @Override
    public BorrowerManager load(String fileName) throws IOException {
        return new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
                .fromJson(new String(Files.readAllBytes(Paths.get(fileName + ".json"))), BorrowerManager.class);
    }

    @Override
    public String getName() {
        return "borrowers_data";
    }
}