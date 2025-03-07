package com.auracoda.dbspringload.Workloads.DeclarativeTriggers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;

public class BusinessLogic implements BusinessLogicInterface {

    final DataSource MyDataSource;
    Connection MyConn;
    Statement MyStmt;

    public BusinessLogic(DataSource dataSource) {
        MyDataSource = dataSource;
        MyConn = null;
        MyStmt = null;
    }

    @Override
    public void PrepareConnectionsAndStatements() throws SQLException {
        MyConn = MyDataSource.getConnection();
        MyStmt = MyConn.createStatement();
    };

    @Override
    public boolean RunBusinessLogic(Map<String, String> MyWorkloadParams) throws Exception {
        // nothing to do...
        return true;
    }

    @Override
    public void TerminateConnections() throws SQLException {
        if (MyStmt != null) {
            MyStmt.close();
        }

        if (MyConn != null) {
            MyConn.close();
        }
    }

}
