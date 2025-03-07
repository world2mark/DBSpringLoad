package com.auracoda.dbspringload.Workloads.ContentionDemo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;

public class BusinessLogic implements BusinessLogicInterface {

    ConcurrentHashMap<String, String> sharedMap = new ConcurrentHashMap<String, String>();

    final DataSource MyDataSource;

    public BusinessLogic(DataSource dataSource) {
        MyDataSource = dataSource;
    }

    @Override
    public void PrepareConnectionsAndStatements() throws SQLException {
    };

    @Override
    public boolean RunBusinessLogic(Map<String, String> MyWorkloadParams) throws Exception {

        // https://github.com/cockroachlabs/cockroachdb-runbook-template/blob/main/diagnostic-support/troubleshooting-sql-contention.md#contention-illustration-32--writes-are-blocking-reads-and-writes

        final String ThreadRunning = "ThreadRunning";
        final String ThreadCompleted = "ThreadCompleted";

        final String StmtMap1 = "StmtMap1";
        sharedMap.put(StmtMap1, ThreadRunning);

        Thread.startVirtualThread(() -> {
            Connection MyConn = null;
            Statement MyStmt = null;
            try {
                MyConn = MyDataSource.getConnection();
                MyConn.setAutoCommit(true);
                MyStmt = MyConn.createStatement();

                MyStmt.executeUpdate("BEGIN");
                MyStmt.executeUpdate("UPDATE contention_demo SET v=2012 WHERE k=2");

                Thread.sleep(500);

                MyStmt.executeUpdate(("COMMIT"));
                System.out.println("Session 1 completed!");
            } catch (Exception myEx) {
                myEx.printStackTrace();
            } finally {
                if (MyStmt != null) {
                    try {
                        MyStmt.close();
                    } catch (SQLException sqlE) {
                        sqlE.printStackTrace();
                    }
                }
                if (MyConn != null) {
                    try {
                        MyConn.close();
                    } catch (SQLException sqlE) {
                        sqlE.printStackTrace();
                    }
                }
                sharedMap.put(StmtMap1, ThreadCompleted);

            }
        });

        Thread.sleep(100);

        final String StmtMap2 = "StmtMap2";
        sharedMap.put(StmtMap2, ThreadRunning);

        Thread.startVirtualThread(() -> {
            Connection MyConn = null;
            Statement MyStmt = null;
            try {
                MyConn = MyDataSource.getConnection();
                MyConn.setAutoCommit(true);
                MyStmt = MyConn.createStatement();

                Thread.sleep(300);

                MyStmt.executeUpdate("BEGIN");
                MyStmt.executeQuery("SELECT * FROM contention_demo WHERE k=2");
                MyStmt.executeUpdate(("COMMIT"));
                System.out.println("Session 2 completed!");
            } catch (Exception myEx) {
                myEx.printStackTrace();
            } finally {
                if (MyStmt != null) {
                    try {
                        MyStmt.close();
                    } catch (SQLException sqlE) {
                        sqlE.printStackTrace();
                    }
                }
                if (MyConn != null) {
                    try {
                        MyConn.close();
                    } catch (SQLException sqlE) {
                        sqlE.printStackTrace();
                    }
                }
                sharedMap.put(StmtMap2, ThreadCompleted);
            }
        });

        Thread.sleep(100);

        final String StmtMap3 = "StmtMap3";
        sharedMap.put(StmtMap3, ThreadRunning);

        Thread.startVirtualThread(() -> {
            Connection MyConn = null;
            Statement MyStmt = null;
            try {
                Thread.sleep(200);

                MyConn = MyDataSource.getConnection();
                MyConn.setAutoCommit(true);
                MyStmt = MyConn.createStatement();

                MyStmt.executeUpdate("BEGIN");
                MyStmt.executeUpdate("UPDATE contention_demo SET v=2032 WHERE k=2");
                MyStmt.executeUpdate(("COMMIT"));
                System.out.println("Session 3 completed!");
            } catch (Exception myEx) {
                myEx.printStackTrace();
            } finally {
                if (MyStmt != null) {
                    try {
                        MyStmt.close();
                    } catch (SQLException sqlE) {
                        sqlE.printStackTrace();
                    }
                }
                if (MyConn != null) {
                    try {
                        MyConn.close();
                    } catch (SQLException sqlE) {
                        sqlE.printStackTrace();
                    }
                }
                sharedMap.put(StmtMap3, ThreadCompleted);
            }
        });

        while (sharedMap.get(StmtMap1).equals(ThreadRunning) ||
                sharedMap.get(StmtMap2).equals(ThreadRunning) ||
                sharedMap.get(StmtMap3).equals(ThreadRunning)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    @Override
    public void TerminateConnections() throws SQLException {
    }

}
