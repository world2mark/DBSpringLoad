package com.auracoda.dbspringload.Workloads;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public class WorkloadService {

        final List<String> TableCreationList = new ArrayList<>();
        final List<String> TableDropList = new ArrayList<>();

        final List<String> populateParameters = new ArrayList<>();

        final List<String> demoEndpoints = new ArrayList<>();

        WorkloadMasterThread masterThread = null;

        public <T> T createInstance(Class<T> clazz, DataSource ds) throws Exception {
                T instance = clazz.getDeclaredConstructor(DataSource.class).newInstance(ds);
                return instance;
        }

        public void AddTableDefinition(
                        String creationDDL,
                        String dropDDL) {
                if (creationDDL != null) {
                        TableCreationList.add(creationDDL);
                }
                if (dropDDL != null) {
                        TableDropList.addFirst(dropDDL); // Reverse order using addFirst (eg: stack) for dropped DDL
                                                         // objects
                }
        }

        public List<String> getPopulateParameters() {
                return populateParameters;
        }

        public void AddPopulateParameters(
                        String newParams) {
                this.populateParameters.add(newParams);
        }

        public List<String> getDemoParameters() {
                return demoEndpoints;
        }

        public void AddDemoParameters(
                        String newParams) {
                demoEndpoints.add(newParams);
        }

        public String getDescription() {
                return "This is an abstract, unimplemented class.  Please derive the specific workload test class from this class.";
        };

        public BusinessLogicInterface CreateBusinessLogicInstance(
                        long threadIndex,
                        DataSource myDataSource) {
                return null;
        }

        public void RunWorkload(
                        long duration,
                        long count,
                        long threads,
                        Map<String, String> workloadParams,
                        DataSource myDataSource,
                        AJAXMessages myMessages) {

                masterThread = new WorkloadMasterThread();

                for (int threadCount = 0; threadCount < threads; threadCount++) {
                        try {
                                // final BusinessLogicInterface myInstance = BusinessLogicClass
                                // .getDeclaredConstructor(DataSource.class).newInstance(
                                // myDataSource,
                                // CreateBusinessLogicInstance(myDataSource));
                                // masterThread.AddThread(myInstance);
                                masterThread.AddThread(CreateBusinessLogicInstance(threadCount, myDataSource));
                        } catch (Exception err) {
                                err.printStackTrace();
                        }
                }

                masterThread.StartWorkload(
                                duration,
                                count,
                                workloadParams);

                myMessages.AddMessage("Running workload");
        }

        public void StopWorkload(
                        AJAXMessages myMessages) {
                if (masterThread != null) {
                        masterThread.StopWorkload();
                }

                // TODO: the actual execution might still be in an "ending" state before it
                // completes.
                masterThread = null;

                myMessages.AddMessage("Workload stopping");
        }

        public void CreateTables(
                        DataSource myDataSource,
                        AJAXMessages myMessages) {
                Connection myConn = null;
                Statement myCreateDDLStmt = null;
                try {
                        boolean allTabledCreated = true;
                        myConn = myDataSource.getConnection();
                        myCreateDDLStmt = myConn.createStatement();
                        for (String createSQLString : TableCreationList) {
                                try {
                                        myCreateDDLStmt.executeUpdate(createSQLString);
                                } catch (SQLException sqlE) {
                                        allTabledCreated = false;
                                        myMessages.AddError(sqlE.getLocalizedMessage());
                                }
                        }
                        if (allTabledCreated) {
                                myMessages.AddMessage("Workload demo objects created successfully.");
                        } else {
                                myMessages.AddError("Not all objects were created.");
                        }
                } catch (SQLException sqlE) {
                        myMessages.AddError(sqlE.getLocalizedMessage());
                } finally {
                        try {
                                if (myCreateDDLStmt != null) {
                                        myCreateDDLStmt.close();
                                }
                                if (myConn != null) {
                                        myConn.close();
                                }
                        } catch (SQLException sqlE) {
                                myMessages.AddError(sqlE.getLocalizedMessage());
                        }
                }
        }

        public void PopulateTables(
                        long rowCount,
                        Map<String, String> workloadParams,
                        DataSource myDataSource,
                        AJAXMessages myMessages) {
                myMessages.AddMessage("Nothing to do");
        }

        public void DropTables(
                        DataSource myDataSource,
                        AJAXMessages myMessages) {

                Connection myConn = null;

                try {
                        boolean allTabledDropped = true;
                        for (String dropSQLString : TableDropList) {
                                Statement myDropDDLStmt = null;
                                try {
                                        if (myConn == null) {
                                                myConn = myDataSource.getConnection();
                                        }
                                        if (myDropDDLStmt == null) {
                                                myDropDDLStmt = myConn.createStatement();
                                        }
                                        myDropDDLStmt.executeUpdate(dropSQLString);
                                } catch (SQLException sqlE) {
                                        if (myDropDDLStmt != null) {
                                                myDropDDLStmt.close();
                                                myDropDDLStmt = null;
                                        }
                                        if (myConn != null) {
                                                myConn.close();
                                                myConn = null;
                                        }
                                        allTabledDropped = false;
                                        myMessages.AddError(sqlE.getLocalizedMessage());
                                } finally {
                                        if (myConn != null) {
                                                myConn.close();
                                                myConn = null;
                                        }
                                }
                        }
                        if (allTabledDropped) {
                                myMessages.AddMessage("Workload demo objects dropped successfully.");
                        } else {
                                myMessages.AddError("Not all objects were dropped.");
                        }
                } catch (

                SQLException sqlE) {
                        myMessages.AddError(sqlE.getLocalizedMessage());
                } finally {
                        try {
                                if (myConn != null) {
                                        myConn.close();
                                }
                        } catch (SQLException sqlE) {
                                myMessages.AddError(sqlE.getLocalizedMessage());
                        }
                }
        }
}
