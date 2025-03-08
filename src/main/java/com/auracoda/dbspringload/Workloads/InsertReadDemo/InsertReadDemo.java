package com.auracoda.dbspringload.Workloads.InsertReadDemo;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;
import com.auracoda.dbspringload.Workloads.WorkloadService;

public class InsertReadDemo extends WorkloadService {

    public InsertReadDemo() {
        AddTableDefinition(
                "create table insert_read (" +
                        "  thekey uuid default gen_random_uuid() primary key," +
                        "  thename string not null, thevalue int not null)",
                "drop table insert_read");

        AddDemoParameters("duration=-1&count=1&threads=1");
        AddDemoParameters("duration=-1&count=10&threads=1");
        AddDemoParameters("duration=10&count=-1&threads=1");
        AddDemoParameters("duration=10&count=-1&threads=4");
    };

    @Override
    public String getDescription() {
        return "This demo tests the workload harness.  It writes one row and immediately reads it";
    }

    @Override
    public BusinessLogicInterface CreateBusinessLogicInstance(
            long threadIndex,
            DataSource myDataSource) throws Exception {
        return new BusinessLogic(myDataSource);
    }

}
