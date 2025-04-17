package com.auracoda.dbspringload.Workloads.ItemHistory;

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
        final StaticDataObjects MyStaticData;

        Connection MyConnection;
        PreparedStatement MySelectStmt;

        public BusinessLogic(
                        DataSource dataSource,
                        StaticDataObjects staticData) {
                MyDataSource = dataSource;
                MyStaticData = staticData;
        }

        @Override
        public void PrepareConnectionsAndStatements(Map<String, String> MyWorkloadParams) throws SQLException {
                MyConnection = MyDataSource.getConnection();

                MyConnection.setAutoCommit(true);

                MySelectStmt = MyConnection.prepareStatement(
                                "SELECT * FROM " +
                                                "item_history_2 AS OF SYSTEM TIME '-0.001ms' " +
                                                "WHERE bucket_id=? AND entry_id LIKE ? AND entry_id>? " +
                                                "ORDER BY entry_id ASC, version_num DESC LIMIT ?");

        };

        @Override
        public boolean RunBusinessLogic(Map<String, String> MyWorkloadParams) throws Exception {

                final int someObjectIndex = MyStaticData.GetRandomObjectIndex();
                final String BucketID = MyStaticData.GetObjectBuckedIDByIndex(someObjectIndex);
                
                final String ObjectLikeKey = MyStaticData.GetObjectKeyByIndex(someObjectIndex) + '%';
                // this is a wildcard prefix, thus we need to supply the '%' at the end;

                final String ObjectKeyStart = WorkloadDataHelpers.RandomAlphanumericString(3);
                // The idea of the length:3 above is to hopefully find some keys that will start with this prefix

                final int MAX_RETRIES = 4;
                for (int retryAttempt = 0; retryAttempt < MAX_RETRIES; retryAttempt++) {
                        try {

                                MySelectStmt.setString(1, BucketID);
                                MySelectStmt.setString(2, ObjectLikeKey);
                                MySelectStmt.setString(3, ObjectKeyStart);
                                MySelectStmt.setInt(4, 1001);

                                final ResultSet rs = MySelectStmt.executeQuery();
                                rs.next();
                                final String returnedBucketID = rs.getString(3); // bucket_id is the 3rd column
                                final String returnedObjectID = rs.getString(4); // bucket_id is the 3rd column
                                if (!BucketID.equals(returnedBucketID) || !returnedObjectID.startsWith(ObjectKeyStart)) {
                                        throw new SQLException("Data does not match");
                                }

                                return true;
                        } catch (SQLException sqlE) {
                                try {
                                        if (sqlE.getSQLState().equals("40001")) {
                                                Thread.sleep((retryAttempt + 1) * 150);
                                        } else {
                                                return false;
                                        }
                                } catch (Exception ex) {
                                        // ex.printStackTrace();
                                }
                        }
                }

                // Too many retries!!
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
