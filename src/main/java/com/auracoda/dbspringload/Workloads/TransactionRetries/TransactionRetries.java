package com.auracoda.dbspringload.Workloads.TransactionRetries;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;
import com.auracoda.dbspringload.Workloads.WorkloadService;

public class TransactionRetries extends WorkloadService {

    public TransactionRetries() {
        AddTableDefinition(
                "CREATE TABLE demo_tr_table (" +
                        "account_id UUID default gen_random_uuid()," +
                        "account_type VARCHAR(20) NOT NULL," +
                        "account_balance DECIMAL(23,5) NOT NULL," +
                        "primary key (account_id))",
                "drop table demo_tr_table");

        AddDemoParameters("threads=1&count=100&duration=-1&RetryFailProbability=0.5");
        AddDemoParameters("threads=1&count=100&duration=-1&RetryFailProbability=0.95");
        AddDemoParameters("threads=5&count=-1&duration=10&RetryFailProbability=0.25");
        AddDemoParameters("threads=5&count=-1&duration=10&RetryFailProbability=0.75");
    };

    @Override
    public String getDescription() {
        return "This demo explores the use of transaction retries by forcing them to occur via session variable.";
    }

    @Override
    public BusinessLogicInterface CreateBusinessLogicInstance(
            long threadIndex,
            DataSource myDataSource) {
        return new BusinessLogic(myDataSource);
    }

}
