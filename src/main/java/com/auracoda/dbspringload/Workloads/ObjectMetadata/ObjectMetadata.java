package com.auracoda.dbspringload.Workloads.ObjectMetadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.AJAXMessages;
import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;
import com.auracoda.dbspringload.Workloads.WorkloadDataHelpers;
import com.auracoda.dbspringload.Workloads.WorkloadService;

public class ObjectMetadata extends WorkloadService {

    public StaticDataObjects staticData = new StaticDataObjects();

    public ObjectMetadata() {

        AddTableDefinition(
                "CREATE TABLE buckets (" +
                        "id UUID DEFAULT gen_random_uuid() primary key" +
                        ")",
                "drop table buckets");

        AddTableDefinition(
                "CREATE TABLE item_history (" +
                        "created_at TIMESTAMPTZ NULL," +
                        "updated_at TIMESTAMPTZ NULL," +
                        "bucket_id UUID NOT NULL," +
                        "entry_id VARCHAR(1024) NOT NULL," +
                        "version_num INT8 NOT NULL DEFAULT (now():::TIMESTAMPTZ::FLOAT8 * 1e+06:::FLOAT8)::INT8," +
                        "null_version BOOL NULL," +
                        "is_available BOOL NULL," +
                        "expired_obj BOOL NULL," +
                        "reference BOOL NULL," +
                        "instance_type INT2 NULL," +
                        "content_id UUID NULL," +
                        "user_address UUID NULL," +
                        "checksum_algorithm STRING NULL," +
                        "checksum STRING NULL," +
                        "est_byte_size INT8 NULL," +
                        "usertags VARCHAR(64) NULL," +
                        "custominfo JSONB NULL," +
                        "checksum_type VARCHAR NULL," +
                        "hashstringid STRING NULL," +
                        "extra_info BYTES NULL," +
                        "PRIMARY KEY (bucket_id ASC, entry_id ASC, version_num ASC)," +
                        "CONSTRAINT fk_buckets_item_history FOREIGN KEY (bucket_id) REFERENCES buckets(id),"
                        +
                        "FAMILY \"primary\" (created_at, updated_at, bucket_id, entry_id, version_num, null_version, is_available, expired_obj, reference, instance_type, content_id, user_address, checksum_algorithm,checksum, est_byte_size, usertags, custominfo, checksum_type, hashstringid,extra_info)"
                        +
                        ")",
                "drop table item_history");

        AddPopulateParameters("rowCount=1&Buckets");
        AddPopulateParameters("rowCount=1000&Buckets");
        AddPopulateParameters("rowCount=100&History");
        AddPopulateParameters("rowCount=100000&History");
        AddPopulateParameters("rowCount=100000&History&threads=4");

        AddDemoParameters("duration=-1&count=1&threads=1");
        AddDemoParameters("duration=90&count=-1&threads=4");
        AddDemoParameters("duration=90&count=-1&threads=16");
        AddDemoParameters("duration=90&count=-1&threads=64");
        AddDemoParameters("duration=90&count=-1&threads=256");
        AddDemoParameters("Cleaner&duration=-1&count=1&threads=1");
        AddDemoParameters("Cleaner&duration=90&count=-1&threads=4");
        AddDemoParameters("Cleaner&duration=90&count=-1&threads=16");
        AddDemoParameters("Cleaner&duration=90&count=-1&threads=64");
        AddDemoParameters("Cleaner&duration=90&count=-1&threads=256");
    };

    @Override
    public String getDescription() {
        return "Demo for optimizing simple queries.";
    }

    @Override
    public void RunWorkload(
            long duration,
            long count,
            long threads,
            Map<String, String> workloadParams,
            DataSource myDataSource,
            AJAXMessages myMessages) {

        staticData.LoadStaticData(myDataSource, myMessages);

        super.RunWorkload(duration,
                count,
                threads,
                workloadParams,
                myDataSource,
                myMessages);
    };

    @Override
    public BusinessLogicInterface CreateBusinessLogicInstance(
            long threadIndex,
            DataSource myDataSource) throws Exception {
        return new BusinessLogic(
                myDataSource,
                staticData);
    }

    @Override
    public void PopulateTables(
            long rowCount,
            Map<String, String> workloadParams,
            DataSource myDataSource,
            AJAXMessages myMessages) {

        if (workloadParams.get("Buckets") != null) {
            PopulateBuckets(rowCount, workloadParams, myDataSource, myMessages);
        }

        if (workloadParams.get("History") != null) {
            PopulateHistory(rowCount, workloadParams, myDataSource, myMessages);
        }
    }

    void PopulateBuckets(
            long rowCount,
            Map<String, String> workloadParams,
            DataSource myDataSource,
            AJAXMessages myMessages) {

        final String optinalThreadsCount = workloadParams.get("threads");
        if(optinalThreadsCount != null) {
            
        }

        final String InsertParameters = "(DEFAULT)";

        final String InsertSQLSingle = "insert into buckets values " + InsertParameters;
        final long BLOCK_SIZE = 20;
        final StringBuffer SB = new StringBuffer();
        SB.append(InsertSQLSingle);
        final String CommaParameters = "," + InsertParameters;
        for (long blockCount = 1; blockCount < BLOCK_SIZE; blockCount++) {
            SB.append(CommaParameters);
        }

        Connection myConn = null;
        PreparedStatement insertBlock = null;
        PreparedStatement insertSingle = null;
        try {
            myConn = myDataSource.getConnection();
            insertBlock = myConn.prepareStatement(SB.toString());
            insertSingle = myConn.prepareStatement(InsertSQLSingle);

            long rowsSaved = 0;

            final long totalBlocks = rowCount / BLOCK_SIZE;
            for (long blockCount = 0; blockCount < totalBlocks; blockCount++) {
                rowsSaved += BLOCK_SIZE;
                insertBlock.executeUpdate();
            }

            for (; rowsSaved < rowCount; rowsSaved++) {
                insertSingle.executeUpdate();
            }
            myMessages.AddMessage("Successfully inserted " + rowsSaved + " row(s).");

            staticData.LoadStaticData(myDataSource, myMessages);

        } catch (SQLException sqlE) {
            myMessages.AddError(sqlE.getLocalizedMessage());
        } finally {
            try {
                if (insertSingle != null) {
                    insertSingle.close();
                }
                if (insertBlock != null) {
                    insertBlock.close();
                }
                if (myConn != null) {
                    myConn.close();
                }
            } catch (SQLException sqlE) {
                myMessages.AddError(sqlE.getLocalizedMessage());
            }
        }
    }

    void PopulateHistory(
            long rowCount,
            Map<String, String> workloadParams,
            DataSource myDataSource,
            AJAXMessages myMessages) {

        staticData.LoadStaticData(myDataSource, myMessages);

        final String InsertParameters = "(?,?,?,?,DEFAULT,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        final String InsertSQLSingle = "insert into item_history values " + InsertParameters;
        final long BLOCK_SIZE = 10;
        final StringBuffer SB = new StringBuffer();
        SB.append(InsertSQLSingle);
        final String CommaParameters = "," + InsertParameters;
        for (long blockCount = 1; blockCount < BLOCK_SIZE; blockCount++) {
            SB.append(CommaParameters);
        }

        Connection myConn = null;
        PreparedStatement insertBlock = null;
        PreparedStatement insertSingle = null;
        try {
            myConn = myDataSource.getConnection();
            insertBlock = myConn.prepareStatement(SB.toString());
            insertSingle = myConn.prepareStatement(InsertSQLSingle);

            Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

            long rowsSaved = 0;

            final long totalBlocks = rowCount / BLOCK_SIZE;
            for (long blockCount = 0; blockCount < totalBlocks; blockCount++) {
                rowsSaved += BLOCK_SIZE;
                int paramIndex = 1;
                for (int nextParam = 0; nextParam < BLOCK_SIZE; nextParam++) {
                    // "created_at TIMESTAMPTZ NULL," +
                    insertBlock.setTimestamp(paramIndex++, nowTimestamp);
                    // "updated_at TIMESTAMPTZ NULL," +
                    insertBlock.setTimestamp(paramIndex++, nowTimestamp);
                    // "bucket_id UUID NOT NULL," +
                    insertBlock.setString(paramIndex++, staticData.GetRandomBucketID());
                    // "entry_id VARCHAR(1024) NOT NULL," +
                    insertBlock.setString(paramIndex++, WorkloadDataHelpers.RandomAlphanumericString(500));
                    // "version_num INT8 NOT NULL DEFAULT (now():::TIMESTAMPTZ::FLOAT8 *
                    // 1e+06:::FLOAT8)::INT8," +
                    // Using DEFAULT
                    // "null_version BOOL NULL," +
                    insertBlock.setBoolean(paramIndex++, true);
                    // "is_available BOOL NULL," +
                    insertBlock.setBoolean(paramIndex++, true);
                    // "expired_obj BOOL NULL," +
                    insertBlock.setBoolean(paramIndex++, true);
                    // "reference BOOL NULL," +
                    insertBlock.setBoolean(paramIndex++, true);
                    // "instance_type INT2 NULL," +
                    insertBlock.setInt(paramIndex++, WorkloadDataHelpers.RandomInt(100, 15000));
                    // "content_id UUID NULL," +
                    insertBlock.setString(paramIndex++, "aaaaaaa1-bbbb-cccc-dddd-eeeeffffeeee");
                    // "user_address UUID NULL," +
                    insertBlock.setString(paramIndex++, "aaaaaaa2-bbbb-cccc-dddd-eeeeffffeeee");
                    // "checksum_algorithm STRING NULL," +
                    insertBlock.setString(paramIndex++, "aaaaaaa3-bbbb-cccc-dddd-eeeeffffeeee");
                    // "checksum STRING NULL," +
                    insertBlock.setString(paramIndex++, "aaaaaaa4-bbbb-cccc-dddd-eeeeffffeeee");
                    // "est_byte_size INT8 NULL," +
                    insertBlock.setInt(paramIndex++, WorkloadDataHelpers.RandomInt(100, 15000));
                    // "usertags VARCHAR(64) NULL," +
                    insertBlock.setString(paramIndex++, WorkloadDataHelpers.RandomAlphanumericString(60));
                    // "custominfo JSONB NULL," +
                    insertBlock.setString(paramIndex++, WorkloadDataHelpers.CreateRandomJSON(10));
                    // "checksum_type VARCHAR NULL," +
                    insertBlock.setString(paramIndex++, WorkloadDataHelpers.RandomAlphanumericString(10));
                    // "hashstringid STRING NULL," +
                    insertBlock.setString(paramIndex++, WorkloadDataHelpers.RandomAlphanumericString(100));
                    // "extra_info BYTES NULL," +
                    insertBlock.setBytes(paramIndex++, WorkloadDataHelpers.CreateRandomBytes(100));
                }
                insertBlock.executeUpdate();
            }

            for (; rowsSaved < rowCount; rowsSaved++) {
                int paramIndex = 1;
                // "created_at TIMESTAMPTZ NULL," +
                insertSingle.setTimestamp(paramIndex++, nowTimestamp);
                // "updated_at TIMESTAMPTZ NULL," +
                insertSingle.setTimestamp(paramIndex++, nowTimestamp);
                // "bucket_id UUID NOT NULL," +
                insertSingle.setString(paramIndex++, staticData.GetRandomBucketID());
                // "entry_id VARCHAR(1024) NOT NULL," +
                insertSingle.setString(paramIndex++, WorkloadDataHelpers.RandomAlphanumericString(500));
                // "version_num INT8 NOT NULL DEFAULT (now():::TIMESTAMPTZ::FLOAT8 *
                // 1e+06:::FLOAT8)::INT8," +
                // Using DEFAULT
                // "null_version BOOL NULL," +
                insertSingle.setBoolean(paramIndex++, true);
                // "is_available BOOL NULL," +
                insertSingle.setBoolean(paramIndex++, true);
                // "expired_obj BOOL NULL," +
                insertSingle.setBoolean(paramIndex++, true);
                // "reference BOOL NULL," +
                insertSingle.setBoolean(paramIndex++, true);
                // "instance_type INT2 NULL," +
                insertSingle.setInt(paramIndex++, WorkloadDataHelpers.RandomInt(100, 15000));
                // "content_id UUID NULL," +
                insertSingle.setString(paramIndex++, "aaaaaaa1-bbbb-cccc-dddd-eeeeffffeeee");
                // "user_address UUID NULL," +
                insertSingle.setString(paramIndex++, "aaaaaaa2-bbbb-cccc-dddd-eeeeffffeeee");
                // "checksum_algorithm STRING NULL," +
                insertSingle.setString(paramIndex++, "aaaaaaa3-bbbb-cccc-dddd-eeeeffffeeee");
                // "checksum STRING NULL," +
                insertSingle.setString(paramIndex++, "aaaaaaa4-bbbb-cccc-dddd-eeeeffffeeee");
                // "est_byte_size INT8 NULL," +
                insertSingle.setInt(paramIndex++, WorkloadDataHelpers.RandomInt(100, 15000));
                // "usertags VARCHAR(64) NULL," +
                insertSingle.setString(paramIndex++, WorkloadDataHelpers.RandomAlphanumericString(60));
                // "custominfo JSONB NULL," +
                insertSingle.setString(paramIndex++, WorkloadDataHelpers.CreateRandomJSON(10));
                // "checksum_type VARCHAR NULL," +
                insertSingle.setString(paramIndex++, WorkloadDataHelpers.RandomAlphanumericString(10));
                // "hashstringid STRING NULL," +
                insertSingle.setString(paramIndex++, WorkloadDataHelpers.RandomAlphanumericString(100));
                // "extra_info BYTES NULL," +
                insertSingle.setBytes(paramIndex++, WorkloadDataHelpers.CreateRandomBytes(100));

                insertSingle.executeUpdate();
            }
            myMessages.AddMessage("Successfully inserted " + rowsSaved + " row(s).");

            staticData.LoadStaticData(myDataSource, myMessages);

        } catch (SQLException sqlE) {
            myMessages.AddError(sqlE.getLocalizedMessage());
        } finally {
            try {
                if (insertSingle != null) {
                    insertSingle.close();
                }
                if (insertBlock != null) {
                    insertBlock.close();
                }
                if (myConn != null) {
                    myConn.close();
                }
            } catch (SQLException sqlE) {
                myMessages.AddError(sqlE.getLocalizedMessage());
            }
        }
    }

}
