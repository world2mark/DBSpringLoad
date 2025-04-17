package com.auracoda.dbspringload.Workloads.ItemHistory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.AJAXMessages;
import com.auracoda.dbspringload.Workloads.WorkloadDataHelpers;

public class StaticDataObjects {

    final Random MyRandom = new Random();

    final List<String> BucketIDs = new ArrayList<>();
    final List<String> ObjectBucketIDs = new ArrayList<>();
    final List<String> ObjectKeys = new ArrayList<>();

    public void LoadStaticData(
            DataSource myDataSource,
            AJAXMessages myMessages) {

        Connection myConn = null;
        Statement myStmt = null;

        try {
            myConn = myDataSource.getConnection();
            myStmt = myConn.createStatement();

            BucketIDs.clear();

            final ResultSet rs1 = myStmt.executeQuery("select id from buckets_2");

            while (rs1.next()) {
                BucketIDs.add(rs1.getString(1));
            }

            ObjectBucketIDs.clear();
            ObjectKeys.clear();

            final ResultSet rs2 = myStmt.executeQuery("select bucket_id,entry_id from item_history_2");

            while (rs2.next()) {
                ObjectBucketIDs.add(rs2.getString(1));
                ObjectKeys.add(rs2.getString(2));
            }

        } catch (SQLException sqlE) {
            myMessages.AddError(sqlE.getLocalizedMessage());
        } finally {
            if (myStmt != null) {
                try {
                    myStmt.close();
                } catch (SQLException sqlE) {
                    myMessages.AddError(sqlE.getLocalizedMessage());
                }
            }
            if (myConn != null) {
                try {
                    myConn.close();
                } catch (SQLException sqlE) {
                    myMessages.AddError(sqlE.getLocalizedMessage());
                }
            }
        }
    }

    public String GetRandomBucketID() {
        return BucketIDs.get(MyRandom.nextInt(BucketIDs.size()));
    }

    public String CreateRandomObjectKey() {
        return WorkloadDataHelpers.RandomAlphanumericString(500);
    }

    public int GetRandomObjectIndex() {
        return MyRandom.nextInt(ObjectBucketIDs.size());
    }

    public String GetObjectBuckedIDByIndex(int index) {
        return ObjectBucketIDs.get(index);
    }

    public String GetObjectKeyByIndex(int index) {
        return ObjectKeys.get(index);
    }

}
