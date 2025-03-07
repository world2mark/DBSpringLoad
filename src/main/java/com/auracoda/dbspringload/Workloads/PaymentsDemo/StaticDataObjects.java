package com.auracoda.dbspringload.Workloads.PaymentsDemo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.AJAXMessages;

public class StaticDataObjects {

    final Random MyRandom = new Random();

    final List<String> AccountBalanceKeys = new ArrayList<>();

    public void LoadStaticData(
            DataSource myDataSource,
            AJAXMessages myMessages) {

        Connection myConn = null;
        Statement myStmt = null;

        try {
            myConn = myDataSource.getConnection();
            myStmt = myConn.createStatement();

            final ResultSet rs = myStmt.executeQuery("select account from account_balance");

            AccountBalanceKeys.clear();

            while (rs.next()) {
                AccountBalanceKeys.add(rs.getString(1));
            }
        } catch (SQLException sqlE) {
            myMessages.AddError(sqlE.getLocalizedMessage());
        } finally {
            if (myStmt != null) {
                try {
                    myStmt.close();
                } catch (SQLException sqlE) {
                    myMessages.AddError(sqlE.getLocalizedMessage());
                }
            }
            if (myConn != null) {
                try {
                    myConn.close();
                } catch (SQLException sqlE) {
                    myMessages.AddError(sqlE.getLocalizedMessage());
                }
            }
        }
    }

    public String GetRandomKey() {
        return AccountBalanceKeys.get(MyRandom.nextInt(AccountBalanceKeys.size()));
    }

    public String GetAnotherRandomKey(String existingKey) {
        int randomIndex = MyRandom.nextInt(AccountBalanceKeys.size());
        String anotherKey = AccountBalanceKeys.get(randomIndex);
        if (existingKey.equals(anotherKey)) {
            randomIndex++;
            if (randomIndex >= AccountBalanceKeys.size()) {
                randomIndex = 0;
            }
            anotherKey = AccountBalanceKeys.get(randomIndex);
        }
        return anotherKey;
    }

    public String GetRandomAccountType() {
        switch (MyRandom.nextInt(6)) {
            case 0:
                return "Savings";
            case 1:
                return "Joint-Savings";
            case 2:
                return "Chequing";
            case 3:
                return "Joint-Chequing";
            case 4:
                return "LOC";
            case 5:
                return "Joint-LOC";
            default:
                return "----null----";
        }
    }

    public String GetRandomTransactionStatus() {
        switch (MyRandom.nextInt(5)) {
            case 0:
                return "Good";
            case 1:
                return "Pending";
            case 2:
                return "Rollback";
            case 3:
                return "ChargeBack";
            case 4:
                return "Cancelled";
            default:
                return "----null----";
        }
    }

    public String GetRandomTransactionType() {
        switch (MyRandom.nextInt(6)) {
            case 0:
                return "Transfer";
            case 1:
                return "POS";
            case 2:
                return "Interac (Email)";
            case 3:
                return "Interac Online";
            case 4:
                return "Mobile Banking";
            case 5:
                return "Refund";
            default:
                return "----null----";
        }
    }
}
