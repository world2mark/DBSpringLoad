package com.auracoda.dbspringload.Workloads.PaymentsDemo;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;
import com.auracoda.dbspringload.Workloads.WorkloadDataHelpers;

public class BusinessLogic implements BusinessLogicInterface {

        final DataSource MyDataSource;
        final StaticDataObjects MyStaticData;

        Connection MyConnection;
        Statement ReadCommittedBeginCommitRollBack;
        PreparedStatement GetAccountDetails;
        PreparedStatement UpdateAccountValueStmt;
        PreparedStatement SaveTransaction;

        public BusinessLogic(
                        DataSource dataSource,
                        StaticDataObjects staticData) {
                MyDataSource = dataSource;
                MyStaticData = staticData;
        }

        @Override
        public void PrepareConnectionsAndStatements(Map<String, String> MyWorkloadParams) throws SQLException {
                MyConnection = MyDataSource.getConnection();

                MyConnection.setAutoCommit(true);

                ReadCommittedBeginCommitRollBack = MyConnection.createStatement();

                String rcEnabled = MyWorkloadParams.get("readCommitted");
                if (rcEnabled == null) {
                        ReadCommittedBeginCommitRollBack
                                        .executeUpdate("SET default_transaction_isolation = 'serializable'");
                } else {
                        ReadCommittedBeginCommitRollBack
                                        .executeUpdate("SET default_transaction_isolation = 'read committed'");
                }
                final ResultSet checkReadCommitted = ReadCommittedBeginCommitRollBack
                                .executeQuery("SHOW default_transaction_isolation");

                checkReadCommitted.next();
                final String readCommittedValue = checkReadCommitted.getString(1);
                if (rcEnabled == null) {
                        if (!readCommittedValue.equals("serializable")) {
                                throw new IllegalStateException(
                                                "We asked for serializable, but received: " + readCommittedValue);
                        }
                } else {
                        if (!readCommittedValue.equals("read committed")) {
                                throw new IllegalStateException(
                                                "We asked for read committed, but received: " + readCommittedValue);
                        }
                }

                GetAccountDetails = MyConnection.prepareStatement("select * from account_balance where account=?");

                UpdateAccountValueStmt = MyConnection
                                .prepareStatement(
                                                "update account_balance set balance=?, reserve=? where account=?");

                SaveTransaction = MyConnection.prepareStatement(
                                "insert into transaction_event values (DEFAULT,?,?,?,?,?,DEFAULT,DEFAULT)");
        };

        private class AccountInfo {
                public String account;
                public String account_type;
                public double balance;
                public double reserve;

                AccountInfo(String acc, String accType, double bal, double res) {
                        account = acc;
                        account_type = accType;
                        balance = bal;
                        reserve = res;
                }
        }

        @Override
        public boolean RunBusinessLogic(Map<String, String> MyWorkloadParams) throws Exception {

                double transactionValue = WorkloadDataHelpers.RandomDouble(25.0, 50.0);

                String mySourceAccount = MyStaticData.GetRandomKey();
                String myTargetAccount = MyStaticData.GetAnotherRandomKey(mySourceAccount);

                final AccountInfo sourceDetails = GetAccountDetails(mySourceAccount);
                // MyConnection.commit();

                final AccountInfo targetDetails = GetAccountDetails(myTargetAccount);
                // MyConnection.commit();

                final int MAX_RETRIES = 4;
                for (int retryAttempt = 0; retryAttempt < MAX_RETRIES; retryAttempt++) {
                        try {
                                ReadCommittedBeginCommitRollBack.executeUpdate("begin");

                                DebitAccount(sourceDetails, transactionValue);

                                CreditAccount(targetDetails, transactionValue);

                                SaveTransaction(sourceDetails, targetDetails, transactionValue);

                                ReadCommittedBeginCommitRollBack.executeUpdate("commit");

                                // MyConnection.commit();

                                return true;
                        } catch (SQLException sqlE) {
                                try {
                                        if (sqlE.getSQLState().equals("40001")) {
                                                // MyConnection.rollback();
                                                ReadCommittedBeginCommitRollBack.executeUpdate("rollback");

                                        }
                                        Thread.sleep((retryAttempt + 1) * 150);
                                } catch (Exception ex) {
                                        // ex.printStackTrace();
                                }
                        }
                }

                // Too many retries!!
                return false;
        }

        AccountInfo GetAccountDetails(String accountID) throws SQLException {
                GetAccountDetails.setString(1, accountID);
                final ResultSet rs = GetAccountDetails.executeQuery();
                rs.next();
                return new AccountInfo(
                                rs.getString(1),
                                rs.getString(3),
                                rs.getDouble(3),
                                rs.getDouble(4));
        };

        void DebitAccount(AccountInfo debitAcctRS, double transValue) throws SQLException {
                // set the account ID
                UpdateAccountValueStmt.setString(3, debitAcctRS.account);
                // Set balance
                UpdateAccountValueStmt.setDouble(1, debitAcctRS.balance - transValue);
                // Set reserve
                UpdateAccountValueStmt.setDouble(2, debitAcctRS.reserve - transValue);

                UpdateAccountValueStmt.executeUpdate();
        }

        void CreditAccount(AccountInfo creditAcctRS, double transValue) throws SQLException {
                // set the account ID
                UpdateAccountValueStmt.setString(3, creditAcctRS.account);
                // Set balance
                UpdateAccountValueStmt.setDouble(1, creditAcctRS.balance + transValue);
                // Set reserve
                UpdateAccountValueStmt.setDouble(2, creditAcctRS.reserve + transValue);

                UpdateAccountValueStmt.executeUpdate();
        }

        void SaveTransaction(AccountInfo mySourceAccount, AccountInfo myTargetAccount, double transactionValue)
                        throws SQLException {
                // transaction_type varchar(35) NOT NULL
                SaveTransaction.setString(1, MyStaticData.GetRandomTransactionType());
                // transaction_status varchar(35) NOT NULL
                SaveTransaction.setString(2, MyStaticData.GetRandomTransactionStatus());
                // valuation NUMERIC(23,2) NOT NULL
                SaveTransaction.setDouble(3, transactionValue);
                // source_acct UUID NOT NULL
                final String debitAccountID = mySourceAccount.account;
                SaveTransaction.setString(4, debitAccountID);
                // target_acct UUID NOT NULL
                final String creditAccountID = myTargetAccount.account;
                SaveTransaction.setString(5, creditAccountID);

                SaveTransaction.executeUpdate();
        }

        @Override
        public void TerminateConnections() throws SQLException {
                if (MyConnection != null) {
                        MyConnection.close();
                        MyConnection = null;
                }
        }

}
