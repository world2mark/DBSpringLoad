package com.auracoda.dbspringload.Workloads.MultipleActivePortals;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public class CommonConnection {

    DataSource MyDataSource;
    Connection currentConnection = null;
    Statement sampleStatment = null;

    int portalInstanceCount = 0;

    void CreateConnIfNotExisting(DataSource myDataSource) throws SQLException {
        MyDataSource = myDataSource;
        if (currentConnection == null) {
            currentConnection = MyDataSource.getConnection();
        }

        if (sampleStatment == null) {
            sampleStatment = currentConnection.createStatement();
        }
        portalInstanceCount++;
    }

    void ExecuteSomeSQL(boolean enableMAP) throws SQLException {
        if (enableMAP) {
            sampleStatment.execute("set multiple_active_portals_enabled to true");
        } else {
            sampleStatment.execute("set multiple_active_portals_enabled to false");
        }

        try {
            sampleStatment.executeUpdate("select pg_sleep(0.5);select * from multipleactiveportals");
        } catch (SQLException sqlE) {
            // Ignore the fact that we are returning mutliepl resultSets.
            final String errorState = sqlE.getSQLState();
            final String errorMsg = sqlE.getMessage();
            if (errorState.equals("0A000")
                    && errorMsg.startsWith("ERROR: unimplemented: multiple active portals is in preview")) {
                throw sqlE;
            } else if (!errorState.equals("0100E")
                    && errorMsg.startsWith("A result was returned when none was expected.")) {
                throw sqlE;
            }
        }

    }

    void TerminateInstance() {
        if (portalInstanceCount > 0) {
            portalInstanceCount--;
        }

        if (portalInstanceCount == 0) {
            try {
                if (sampleStatment != null) {
                    sampleStatment.close();
                    sampleStatment = null;
                }
            } catch (SQLException sqlE) {
                // sqlE.printStackTrace();
            }
            try {
                if (currentConnection != null) {
                    currentConnection.close();
                    currentConnection = null;
                }
            } catch (SQLException sqlE) {
                // sqlE.printStackTrace();
            }
        }
    }
}
