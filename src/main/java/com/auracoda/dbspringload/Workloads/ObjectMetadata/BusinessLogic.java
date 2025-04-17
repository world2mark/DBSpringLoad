package com.auracoda.dbspringload.Workloads.ObjectMetadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;

public class BusinessLogic implements BusinessLogicInterface {

        final DataSource MyDataSource;
        final StaticDataObjects MyStaticData;

        Connection MyConnection;
        PreparedStatement MySelectStmtOrig;
        PreparedStatement MySelectStmtCleaner;

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

                MySelectStmtOrig = MyConnection.prepareStatement(
                                "SELECT\n" + //
                                                "  *\n" + //
                                                "FROM\n" + //
                                                "  item_history\n" + //
                                                "WHERE\n" + //
                                                "  item_history.bucket_id = ? AND item_history.entry_id = ?\n"
                                                + //
                                                "ORDER BY\n" + //
                                                "  version_num DESC\n" + //
                                                "LIMIT\n" + //
                                                "  ?");

                MySelectStmtCleaner = MyConnection.prepareStatement(
                                "SELECT * FROM item_history WHERE item_history.bucket_id=? AND item_history.entry_id=?");
        };

        @Override
        public boolean RunBusinessLogic(Map<String, String> MyWorkloadParams) throws Exception {

                final int someObjectIndex = MyStaticData.GetRandomObjectIndex();
                final String BucketID = MyStaticData.GetObjectBuckedIDByIndex(someObjectIndex);
                final String ObjectKey = MyStaticData.GetObjectKeyByIndex(someObjectIndex);

                if (MyWorkloadParams.get("Cleaner") == null) {

                        final int MAX_RETRIES = 4;
                        for (int retryAttempt = 0; retryAttempt < MAX_RETRIES; retryAttempt++) {
                                try {

                                        MySelectStmtOrig.setString(1, BucketID);
                                        MySelectStmtOrig.setString(2, ObjectKey);
                                        MySelectStmtOrig.setInt(3, 1);

                                        final ResultSet rs = MySelectStmtOrig.executeQuery();
                                        rs.next();
                                        final String returnedBucketID = rs.getString(3); // bucket_id is the 3rd column
                                        final String returnedObjectID = rs.getString(4); // bucket_id is the 3rd column
                                        if (!BucketID.equals(returnedBucketID) || !ObjectKey.equals(returnedObjectID)) {
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
                } else {
                        final int MAX_RETRIES = 4;
                        for (int retryAttempt = 0; retryAttempt < MAX_RETRIES; retryAttempt++) {
                                try {

                                        MySelectStmtCleaner.setString(1, BucketID);
                                        MySelectStmtCleaner.setString(2, ObjectKey);

                                        final ResultSet rs = MySelectStmtCleaner.executeQuery();
                                        rs.next();
                                        final String returnedBucketID = rs.getString(3); // bucket_id is the 3rd column
                                        final String returnedObjectID = rs.getString(4); // bucket_id is the 3rd column
                                        if (!BucketID.equals(returnedBucketID) || !ObjectKey.equals(returnedObjectID)) {
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
        }

        @Override
        public void TerminateConnections() throws SQLException {
                if (MyConnection != null) {
                        MyConnection.close();
                        MyConnection = null;
                }
        }

}
