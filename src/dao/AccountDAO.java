package dao;

import model.Account;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    // ---------- CREATE ACCOUNT ----------
    public int createAccount(String holderName, BigDecimal initialBalance) {
        String sql = "INSERT INTO accounts (holder_name, balance) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, holderName);
            ps.setBigDecimal(2, initialBalance);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error creating account: " + e.getMessage());
        }
        return -1;
    }

    // ---------- GET ACCOUNT ----------
    public Account getAccount(int accNo) {
        String sql = "SELECT * FROM accounts WHERE acc_no = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accNo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                            rs.getInt("acc_no"),
                            rs.getString("holder_name"),
                            rs.getBigDecimal("balance"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching account: " + e.getMessage());
        }
        return null;
    }

    // ---------- DEPOSIT (transaction) ----------
    public boolean deposit(int accNo, BigDecimal amount) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (!credit(con, accNo, amount)) {
                    con.rollback();
                    return false;
                }
                recordTransaction(con, accNo, "DEPOSIT", amount);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Deposit failed: " + e.getMessage());
            return false;
        }
    }

    // ---------- WITHDRAW (transaction) ----------
    public boolean withdraw(int accNo, BigDecimal amount) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (!debit(con, accNo, amount)) {
                    con.rollback();
                    return false;
                }
                recordTransaction(con, accNo, "WITHDRAW", amount);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Withdraw failed: " + e.getMessage());
            return false;
        }
    }

    // ---------- TRANSFER (transaction) ----------
    public boolean transfer(int fromAcc, int toAcc, BigDecimal amount) {
        if (fromAcc == toAcc) {
            System.out.println("Cannot transfer to the same account.");
            return false;
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (!debit(con, fromAcc, amount) || !credit(con, toAcc, amount)) {
                    con.rollback();
                    return false;
                }
                recordTransaction(con, fromAcc, "TRANSFER_OUT", amount);
                recordTransaction(con, toAcc, "TRANSFER_IN", amount);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Transfer failed: " + e.getMessage());
            return false;
        }
    }

    // ---------- MINI STATEMENT ----------
    public List<String> getMiniStatement(int accNo) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT type, amount, txn_time FROM transactions "
                   + "WHERE acc_no = ? ORDER BY txn_id DESC LIMIT 5";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accNo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getTimestamp("txn_time") + " | "
                           + rs.getString("type") + " | ₹"
                           + rs.getBigDecimal("amount"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching statement: " + e.getMessage());
        }
        return list;
    }

    // ================= PRIVATE HELPERS =================

    // Paise kapto. Balance kami asel kinva account nasel tar false.
    private boolean debit(Connection con, int accNo, BigDecimal amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance - ? "
                   + "WHERE acc_no = ? AND balance >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, accNo);
            ps.setBigDecimal(3, amount);
            return ps.executeUpdate() > 0;
        }
    }

    // Paise jama karto. Account nasel tar false.
    private boolean credit(Connection con, int accNo, BigDecimal amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE acc_no = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, amount);
            ps.setInt(2, accNo);
            return ps.executeUpdate() > 0;
        }
    }

    // Transaction history madhe ek row taakto.
    private void recordTransaction(Connection con, int accNo, String type, BigDecimal amount)
            throws SQLException {
        String sql = "INSERT INTO transactions (acc_no, type, amount) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accNo);
            ps.setString(2, type);
            ps.setBigDecimal(3, amount);
            ps.executeUpdate();
        }
    }
}