import dao.AccountDAO;
import model.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static boolean isValidAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Amount must be greater than 0.");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        AccountDAO dao = new AccountDAO();

        while (true) {
            System.out.println("\n===== BANK MANAGEMENT =====");
            System.out.println("1. Create Account");
            System.out.println("2. Check Balance");
            System.out.println("3. Deposit");
            System.out.println("4. Withdraw");
            System.out.println("5. Transfer");
            System.out.println("6. Mini Statement");
            System.out.println("7. Exit");
            System.out.print("Choose: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Holder name: ");
                    String name = sc.nextLine();
                    System.out.print("Opening balance: ");
                    BigDecimal opening = sc.nextBigDecimal();
                    if (opening.compareTo(BigDecimal.ZERO) < 0) {
                        System.out.println("Balance cannot be negative.");
                        break;
                    }
                    int accNo = dao.createAccount(name, opening);
                    if (accNo != -1) {
                        System.out.println("Account created! Your account number: " + accNo);
                    }
                    break;

                case 2:
                    System.out.print("Account number: ");
                    Account acc = dao.getAccount(sc.nextInt());
                    System.out.println(acc != null ? acc : "Account not found.");
                    break;

                case 3:
                    System.out.print("Account number: ");
                    int depAcc = sc.nextInt();
                    System.out.print("Amount: ");
                    BigDecimal depAmt = sc.nextBigDecimal();
                    if (!isValidAmount(depAmt)) break;
                    System.out.println(dao.deposit(depAcc, depAmt)
                            ? "Deposit successful." : "Deposit failed. Account not found?");
                    break;

                case 4:
                    System.out.print("Account number: ");
                    int wAcc = sc.nextInt();
                    System.out.print("Amount: ");
                    BigDecimal wAmt = sc.nextBigDecimal();
                    if (!isValidAmount(wAmt)) break;
                    System.out.println(dao.withdraw(wAcc, wAmt)
                            ? "Withdrawal successful."
                            : "Withdrawal failed. Insufficient balance or account not found.");
                    break;

                case 5:
                    System.out.print("From account: ");
                    int from = sc.nextInt();
                    System.out.print("To account: ");
                    int to = sc.nextInt();
                    System.out.print("Amount: ");
                    BigDecimal tAmt = sc.nextBigDecimal();
                    if (!isValidAmount(tAmt)) break;
                    System.out.println(dao.transfer(from, to, tAmt)
                            ? "Transfer successful."
                            : "Transfer failed. Nothing was changed.");
                    break;

                case 6:
                    System.out.print("Account number: ");
                    List<String> statement = dao.getMiniStatement(sc.nextInt());
                    if (statement.isEmpty()) {
                        System.out.println("No transactions found.");
                    } else {
                        statement.forEach(System.out::println);
                    }
                    break;

                case 7:
                    System.out.println("Thank you!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}