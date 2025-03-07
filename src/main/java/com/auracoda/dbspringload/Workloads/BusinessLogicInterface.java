package com.auracoda.dbspringload.Workloads;

import java.sql.SQLException;
import java.util.Map;

public interface BusinessLogicInterface {

    void PrepareConnectionsAndStatements() throws SQLException;

    boolean RunBusinessLogic(Map<String, String> MyWorkloadParams) throws Exception;

    void TerminateConnections() throws SQLException;
}
