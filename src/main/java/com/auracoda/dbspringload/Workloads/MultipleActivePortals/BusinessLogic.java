package com.auracoda.dbspringload.Workloads.MultipleActivePortals;

import java.sql.SQLException;
import java.util.Map;

import com.auracoda.dbspringload.Workloads.BusinessLogicInterface;

public class BusinessLogic implements BusinessLogicInterface {

    CommonConnection MyCommonConnection;

    public BusinessLogic(CommonConnection myCommonConnection) {
        MyCommonConnection = myCommonConnection;
    }

    @Override
    public void PrepareConnectionsAndStatements(Map<String, String> MyWorkloadParams) throws SQLException {
    };

    @Override
    public boolean RunBusinessLogic(Map<String, String> MyWorkloadParams) throws Exception {
        try {
            boolean enableMAP = false;
            if (MyWorkloadParams.get("enableMAP") != null) {
                enableMAP = true;
            }
            MyCommonConnection.ExecuteSomeSQL(enableMAP);
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void TerminateConnections() throws SQLException {
        MyCommonConnection.TerminateInstance();
    }

}
