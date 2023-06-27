package economy.pcconomy.backend.bank.scripts;

import economy.pcconomy.PcConomy;
import economy.pcconomy.backend.bank.interfaces.IMoney;
import economy.pcconomy.backend.bank.objects.Borrower;
import economy.pcconomy.backend.bank.objects.Loan;
import economy.pcconomy.backend.scripts.BalanceManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class LoanManager {
    public static double trustCoefficient = 1.5d;

    public static double getPercent(double amount, double duration) {
        // Выдать процент под параметры
        return Math.round((PcConomy.GlobalBank.getUsefulAmountOfBudget() / (amount * duration)) * 1000d) / 1000d;
    }

    public static double getDailyPayment(double amount, double duration, double percent) {
        // Выдать дневной платёж по параметрам
        return (amount + amount * (percent / 100d)) / duration;
    }

    public static double getSafetyFactor(double amount, int duration, Borrower borrower) {
        var expired = 0;
        if (borrower == null) return ((duration / 100d)) /
                (expired + (amount / PcConomy.GlobalBank.getUsefulAmountOfBudget()));

        for (Loan loan: borrower.CreditHistory)
            expired += loan.expired;

        return (borrower.CreditHistory.size() + (duration / 100d)) /
                (expired + (amount / PcConomy.GlobalBank.getUsefulAmountOfBudget()));
    }

    public static boolean isSafeLoan(double loanAmount, int duration, Player borrower) {
        return (getSafetyFactor(loanAmount, duration,
                PcConomy.GlobalBorrowerManager.getBorrowerObject(borrower)) >= trustCoefficient); // коэффициент надёжности
    }

    public static void createLoan(double amount, int duration, Player player, List<Loan> Credit, IMoney moneyGiver) {
        // Создание кредита на игрока
        var percentage = LoanManager.getPercent(amount, duration); // процент по кредиту
        var dailyPayment = LoanManager.getDailyPayment(amount, duration, percentage); // дневной платёж

        Credit.add(new Loan(amount + amount * percentage, percentage, duration, dailyPayment, player));

        moneyGiver.changeBudget(-amount);
        new BalanceManager().giveMoney(amount, player);
    }

    public static void payOffADebt(Player player, IMoney creditOwner) {
        var balance = new BalanceManager();
        var loan = getLoan(player.getUniqueId(), creditOwner);

        if (loan == null) return;
        if (balance.notSolvent(loan.amount, player)) return;

        balance.takeMoney(loan.amount, player);
        creditOwner.changeBudget(loan.amount);
        destroyLoan(player.getUniqueId(), creditOwner);
    }

    public static void takePercentFromBorrowers(IMoney moneyTaker) {
        for (Loan loan: moneyTaker.getCreditList()) {
            if (loan.amount <= 0) {
                destroyLoan(loan.Owner, moneyTaker);
                return;
            }

            var balanceWorker = new BalanceManager();
            if (balanceWorker.notSolvent(loan.dailyPayment, Bukkit.getPlayer(loan.Owner)))
                loan.expired += 1;

            balanceWorker.takeMoney(loan.dailyPayment, Bukkit.getPlayer(loan.Owner));
            loan.amount -= loan.dailyPayment;

            moneyTaker.changeBudget(loan.dailyPayment);
        }
    }

    public static Loan getLoan(UUID player, IMoney creditOwner) {
        for (Loan loan: creditOwner.getCreditList())
            if (loan.Owner.equals(player)) return loan;

        return null;
    }

    public static void destroyLoan(UUID player, IMoney creditOwner) {
        var credit = creditOwner.getCreditList();
        var loan = getLoan(player, creditOwner);
        var borrower = PcConomy.GlobalBorrowerManager.getBorrowerObject(Bukkit.getPlayer(player));

        if (borrower != null) {
            borrower.CreditHistory.add(loan);
            PcConomy.GlobalBorrowerManager.setBorrowerObject(borrower);
        } else
            PcConomy.GlobalBorrowerManager.borrowers.add(new Borrower(Bukkit.getPlayer(player), loan));

        credit.remove(getLoan(player, creditOwner));
    }
}
