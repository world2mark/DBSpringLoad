package com.auracoda.dbspringload.Workloads.ContentionDemo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.AJAXMessages;
import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;
import com.auracoda.dbspringload.Workloads.WorkloadService;

public class ContentionDemo extends WorkloadService {

    public ContentionDemo() {
        AddTableDefinition(
                "create table contention_demo (k int primary key, v int)",
                "drop table contention_demo");

        AddPopulateParameters("values=(1,1),(2,2),(3,3)");

        AddDemoParameters("duration=-1&count=1&threads=1");
    };

    @Override
    public String getDescription() {
        return "Academic exercise: This is an implementation of Alex Entin&apos;s runbook on the various types of contention that may occur in a live environment. "
                +
                "The business logic is specific to the write-blocking example, as illustrated in <a target=\"#\" href=\"https://github.com/cockroachlabs/cockroachdb-runbook-template/blob/main/diagnostic-support/troubleshooting-sql-contention.md#contention-illustration-32--writes-are-blocking-reads-and-writes\">this section of the runbook</a>.";
    };

    @Override
    public BusinessLogicInterface CreateBusinessLogicInstance(
            long threadIndex,
            DataSource myDataSource) throws Exception {
        return new BusinessLogic(myDataSource);
    }

    @Override
    public void PopulateTables(
            long rowCount,
            Map<String, String> workloadParams,
            DataSource myDataSource,
            AJAXMessages myMessages) {

        Connection myConn = null;
        Statement myInsert = null;

        try {
            final String insertValues = workloadParams.get("values");
            myConn = myDataSource.getConnection();
            myInsert = myConn.createStatement();
            myInsert.executeUpdate("insert into contention_demo values " + insertValues);
            myMessages.AddMessage("Inserted rows: " + insertValues);
        } catch (Exception myEx) {
            myMessages.AddError(myEx.getLocalizedMessage());
        } finally {
            try {
                if (myInsert != null) {
                    myInsert.close();
                }
                if (myConn != null) {
                    myConn.close();
                }
            } catch (SQLException myEx) {
                myMessages.AddError(myEx.getLocalizedMessage());
            }
        }
    }
}
