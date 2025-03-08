package com.auracoda.dbspringload.Workloads.PaymentsDemo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.AJAXMessages;
import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;
import com.auracoda.dbspringload.Workloads.WorkloadDataHelpers;
import com.auracoda.dbspringload.Workloads.WorkloadService;

// https://www.cockroachlabs.com/docs/stable/read-committed#set-the-default-isolation-level-to-read-committed

public class PaymentsDemo extends WorkloadService {

    public StaticDataObjects staticData = new StaticDataObjects();

    public PaymentsDemo() {
        AddTableDefinition(
                "CREATE TABLE account_balance (" +
                        "account UUID primary key default gen_random_uuid()," +
                        "account_type VARCHAR(20) NOT NULL," +
                        "balance DECIMAL(23,2) NOT NULL," +
                        "reserve DECIMAL(23,2) NOT NULL)",
                "drop table account_balance");

        AddTableDefinition(
                "CREATE TABLE transaction_event (" +
                        "exchange_trans_uid UUID primary key default gen_random_uuid()," +
                        "transaction_type varchar(35) NOT NULL," +
                        "transaction_status varchar(35) NOT NULL," +
                        "valuation NUMERIC(23,2) NOT NULL," +
                        "source_acct UUID NOT NULL," +
                        "target_acct UUID NOT NULL," +
                        "created_on timestamp NOT NULL DEFAULT now()," +
                        "expired_at TIMESTAMPTZ NOT NULL DEFAULT now() + '10 days'," +
                        "INDEX source_acct_trans_idx (source_acct ASC, created_on ASC) STORING (transaction_type, transaction_status, target_acct),"
                        +
                        "INDEX target_acct_trans_idx (target_acct ASC, created_on ASC) STORING (transaction_type, transaction_status, source_acct))",
                "drop table transaction_event");

        AddTableDefinition(
                "CREATE TABLE warning_on_reserve (" +
                        "exchange_trans_uid UUID primary key default gen_random_uuid()," +
                        "transaction_type varchar(35) NOT NULL," +
                        "valuation NUMERIC(23,5) NOT NULL," +
                        "source_acct varchar(70) NOT NULL," +
                        "target_acct varchar(70) NOT NULL," +
                        "actual_result varchar(35) NOT NULL," +
                        "occurrence_timestamp timestamp NOT NULL DEFAULT now())",
                "drop table warning_on_reserve");

        AddPopulateParameters("rowCount=25");
        AddPopulateParameters("rowCount=1000");

        AddDemoParameters("duration=-1&count=1&threads=1");
        AddDemoParameters("duration=90&count=-1&threads=4");
        AddDemoParameters("duration=90&count=-1&threads=16");
        AddDemoParameters("duration=90&count=-1&threads=64");
        AddDemoParameters("duration=90&count=-1&threads=256");

        AddDemoParameters("readCommitted&duration=-1&count=1&threads=1");
        AddDemoParameters("readCommitted&duration=90&count=-1&threads=4");
        AddDemoParameters("readCommitted&duration=90&count=-1&threads=16");
        AddDemoParameters("readCommitted&duration=90&count=-1&threads=64");
        AddDemoParameters("readCommitted&duration=90&count=-1&threads=256");
    };

    @Override
    public String getDescription() {
        return "This runs a payment process transaction.  It reads credit and debit accounts, updates the balances, and finally commits a transaction. This demo has endpoints for both <b>serializable</b> and <b>read-committed</b> isolation-level transactions.  Note that this is a write-heavy transaction so RC has little to no impact.";
    }

    @Override
    public void RunWorkload(
            long duration,
            long count,
            long threads,
            Map<String, String> workloadParams,
            DataSource myDataSource,
            AJAXMessages myMessages) {

        staticData.LoadStaticData(myDataSource, myMessages);

        super.RunWorkload(duration,
                count,
                threads,
                workloadParams,
                myDataSource,
                myMessages);
    };

    @Override
    public BusinessLogicInterface CreateBusinessLogicInstance(
            long threadIndex,
            DataSource myDataSource) throws Exception {
        return new BusinessLogic(
                myDataSource,
                staticData);
    }

    @Override
    public void PopulateTables(
            long rowCount,
            Map<String, String> workloadParams,
            DataSource myDataSource,
            AJAXMessages myMessages) {

        final String InsertParameters = "(DEFAULT,?,?,?)";

        final String InsertSQLSingle = "insert into account_balance values " + InsertParameters;
        final long BLOCK_SIZE = 20;
        final StringBuffer SB = new StringBuffer();
        SB.append(InsertSQLSingle);
        final String CommaParameters = "," + InsertParameters;
        for (long blockCount = 1; blockCount < BLOCK_SIZE; blockCount++) {
            SB.append(CommaParameters);
        }

        Connection myConn = null;
        PreparedStatement insertBlock = null;
        PreparedStatement insertSingle = null;
        try {
            myConn = myDataSource.getConnection();
            insertBlock = myConn.prepareStatement(SB.toString());
            insertSingle = myConn.prepareStatement(InsertSQLSingle);

            long rowsSaved = 0;

            final long totalBlocks = rowCount / BLOCK_SIZE;
            for (long blockCount = 0; blockCount < totalBlocks; blockCount++) {
                rowsSaved += BLOCK_SIZE;
                int paramIndex = 1;
                for (int nextParam = 0; nextParam < BLOCK_SIZE; nextParam++) {
                    // account_type VARCHAR(10) NOT NULL
                    insertBlock.setString(paramIndex++, staticData.GetRandomAccountType());
                    // account_balance DECIMAL(23,5) NOT NULL
                    insertBlock.setDouble(paramIndex++, WorkloadDataHelpers.RandomDouble(1000.0, 100000.0));
                    // reserve_balance DECIMAL(23,5) NOT NULL
                    insertBlock.setDouble(paramIndex++, WorkloadDataHelpers.RandomDouble(100.0, 100000.0));
                }
                insertBlock.executeUpdate();
            }

            for (; rowsSaved < rowCount; rowsSaved++) {
                int paramIndex = 1;
                // account_type VARCHAR(10) NOT NULL
                insertSingle.setString(paramIndex++, staticData.GetRandomAccountType());
                // account_balance DECIMAL(23,5) NOT NULL
                insertSingle.setDouble(paramIndex++, WorkloadDataHelpers.RandomDouble(1000.0, 100000.0));
                // reserve_balance DECIMAL(23,5) NOT NULL
                insertSingle.setDouble(paramIndex++, WorkloadDataHelpers.RandomDouble(100.0, 100000.0));
                insertSingle.executeUpdate();
            }
            myMessages.AddMessage("Successfully inserted " + rowsSaved + " row(s).");

            staticData.LoadStaticData(myDataSource, myMessages);

        } catch (SQLException sqlE) {
            myMessages.AddError(sqlE.getLocalizedMessage());
        } finally {
            try {
                if (insertSingle != null) {
                    insertSingle.close();
                }
                if (insertBlock != null) {
                    insertBlock.close();
                }
                if (myConn != null) {
                    myConn.close();
                }
            } catch (SQLException sqlE) {
                myMessages.AddError(sqlE.getLocalizedMessage());
            }
        }
    }

}
