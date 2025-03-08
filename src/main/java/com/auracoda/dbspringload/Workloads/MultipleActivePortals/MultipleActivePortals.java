package com.auracoda.dbspringload.Workloads.MultipleActivePortals;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;
import com.auracoda.dbspringload.Workloads.WorkloadService;

public class MultipleActivePortals extends WorkloadService {

        CommonConnection myCommonConnection = new CommonConnection();

        public MultipleActivePortals() {
                AddTableDefinition(
                                "create table multipleactiveportals (myvalue int)",
                                "drop table multipleactiveportals");

                AddDemoParameters("duration=-1&count=1&threads=1");
                AddDemoParameters("duration=-1&count=1&threads=1&enableMAP");
        };

        @Override
        public String getDescription() {
                return "Validation of <b>multiple active portals</b> capability for CockroachDB. CRDB does <b>NOT</b> support MAP unless explicitly enabled at the session level. You will see the error: <span style=\"font-weight: bolder;color: darkred;\">ERROR: unimplemented: multiple active portals is in preview, please set session variable multiple_active_portals_enabled to true to enable them</span> using the first endpoint. The second endpoint sets the session to allow MAP, resulting in a successful business logic workflow across multiple threads.";
        }

        @Override
        public BusinessLogicInterface CreateBusinessLogicInstance(
                        long threadIndex,
                        DataSource myDataSource) throws Exception {
                myCommonConnection.CreateConnIfNotExisting(myDataSource);
                return new BusinessLogic(myCommonConnection);
        }

}
