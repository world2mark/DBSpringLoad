package com.auracoda.dbspringload.Workloads.TransactionRetries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;
import com.auracoda.dbspringload.Workloads.WorkloadDataHelpers;

public class BusinessLogic implements BusinessLogicInterface {

    final static int MAX_RETRIES = 1;

    DataSource MyDataSource;

    Connection MyConnection;
    PreparedStatement InjectRetryErrorStmt;
    PreparedStatement InsertStatement;
    PreparedStatement SelectStatement;

    public BusinessLogic(DataSource dataSource) {
        MyDataSource = dataSource;
    }

    @Override
    public void PrepareConnectionsAndStatements() throws SQLException {
        MyConnection = MyDataSource.getConnection();
        InsertStatement = MyConnection
                .prepareStatement(
                        "insert into demo_tr_table (account_type, account_balance) values (?,?) returning account_id");

        SelectStatement = MyConnection
                .prepareStatement("select account_type, account_balance from demo_tr_table where account_id=?");

        InjectRetryErrorStmt = MyConnection.prepareStatement("set inject_retry_errors_enabled = ?");
    };

    @Override
    public boolean RunBusinessLogic(Map<String, String> MyWorkloadParams) throws Exception {

        double retryFailProbability = Double.valueOf(MyWorkloadParams.get("RetryFailProbability"));

        // Here is the magic, if the probability is a retry failure, we will fail it.
        if (retryFailProbability >= Math.random()) {
            InjectRetryErrorStmt.setString(1, "true");
        } else {
            InjectRetryErrorStmt.setString(1, "false");
        }
        InjectRetryErrorStmt.executeUpdate();

        MyConnection.setAutoCommit(false);

        long RetryBackoffTimeMS = 0;

        for (int retryAttempt = 0; retryAttempt < MAX_RETRIES; retryAttempt++) {
            if (RetryBackoffTimeMS > 0) {
                try {
                    Thread.sleep(RetryBackoffTimeMS);
                } catch (InterruptedException e) {
                    // disregard any interruptions
                }
            }

            try {

                final String insertedString = WorkloadDataHelpers.RandomAlphanumericString(20);
                InsertStatement.setString(1, insertedString);

                final double insertedValue = WorkloadDataHelpers.RandomInt(10, 20);
                InsertStatement.setDouble(2, insertedValue);

                final ResultSet rsInsert = InsertStatement.executeQuery();

                MyConnection.commit();

                rsInsert.next();

                final String insertedKey = rsInsert.getString("account_id");
                SelectStatement.setString(1, insertedKey);

                final ResultSet rsSelect = SelectStatement.executeQuery();

                rsSelect.next();

                final String returnedString = rsSelect.getString("account_type");
                if (!insertedString.equals(returnedString)) {
                    throw new SQLException("data mismatch (account_type)");
                }

                final double returnedValue = rsSelect.getDouble("account_balance");
                if (insertedValue != returnedValue) {
                    throw new SQLException("data mismatch (account_balance)");
                }

                // Upon a successful transaction, we short-circuit any further exception checks
                // and exit the transaction workflow
                return true;
            } catch (SQLException sqlE) {
                if (sqlE.getSQLState().equals("40001")) {
                    MyConnection.rollback();
                    // Each time we run into a retry error,
                    // add an additional 250 ms to the back-off wait time
                    RetryBackoffTimeMS += 250;
                } else {
                    // All other exceptions shoudl be treated as failures
                    throw sqlE;
                }
            }
        }
        // Too many retries have occurred. In production apps you would throw an
        // exeption, eg:
        // throw new IllegalStateException("Too many retries");
        // but for the purposes of this test-harness, we simply return false to indicate
        // to the master-thread that we successfully "failed" this workflow.
        return false;
    }

    @Override
    public void TerminateConnections() throws SQLException {
        if (MyConnection != null) {
            MyConnection.close();
            MyConnection = null;
        }
    }

}
