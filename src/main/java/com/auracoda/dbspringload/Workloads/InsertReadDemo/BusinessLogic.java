package com.auracoda.dbspringload.Workloads.InsertReadDemo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;
import com.auracoda.dbspringload.Workloads.WorkloadDataHelpers;

public class BusinessLogic implements BusinessLogicInterface {

    final DataSource MyDataSource;

    Connection MyConnection;
    PreparedStatement InsertStatement;
    PreparedStatement SelectStatement;

    public BusinessLogic(DataSource dataSource) {
        MyDataSource = dataSource;
    }

    @Override
    public void PrepareConnectionsAndStatements(Map<String, String> MyWorkloadParams) throws SQLException {
        MyConnection = MyDataSource.getConnection();
        InsertStatement = MyConnection
                .prepareStatement(
                        "insert into insert_read (thename, thevalue) values (?,?) returning thekey");

        SelectStatement = MyConnection
                .prepareStatement("select thename, thevalue from insert_read where thekey=?");

    };

    @Override
    public boolean RunBusinessLogic(Map<String, String> MyWorkloadParams) throws Exception {
        final String insertedString = WorkloadDataHelpers.RandomAlphanumericString(20);
        InsertStatement.setString(1, insertedString);

        final int insertedInt = WorkloadDataHelpers.RandomInt(10, 20);
        InsertStatement.setInt(2, insertedInt);

        final ResultSet rsInsert = InsertStatement.executeQuery();

        rsInsert.next();

        final String insertedKey = rsInsert.getString("thekey");

        // try {
        //     Thread.sleep(100);
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }

        SelectStatement.setString(1, insertedKey);

        final ResultSet rsSelect = SelectStatement.executeQuery();

        rsSelect.next();

        final String returnedString = rsSelect.getString("thename");
        if (!insertedString.equals(returnedString)) {
            throw new SQLException("data mismatch (thename)");
        }

        final int returnedInt = rsSelect.getInt("thevalue");
        if (insertedInt != returnedInt) {
            throw new SQLException("data mismatch (thevalue)");
        }

        return true;
    }

    @Override
    public void TerminateConnections() throws SQLException {
        if (MyConnection != null) {
            MyConnection.close();
            MyConnection = null;
        }
    }

}
