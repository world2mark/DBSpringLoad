package com.auracoda.dbspringload.Workloads;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public interface WorkloadInterface {

        String getDescription();

        public BusinessLogicInterface CreateBusinessLogicInstance(
                        DataSource myDataSource) throws Exception;

        public void CreateTables(
                        DataSource myDataSource,
                        AJAXMessages myMessages);

        public void DropTables(
                        DataSource myDataSource,
                        AJAXMessages myMessages);

        public void PopulateTables(
                        long rowCount,
                        Map<String, String> workloadParams,
                        DataSource myDataSource,
                        AJAXMessages myMessages);

        public void RunWorkload(
                        long duration,
                        long count,
                        long threads,
                        Map<String, String> workloadParams,
                        DataSource myDataSource,
                        AJAXMessages myMessages);

        public void StopWorkload(
                        AJAXMessages myMessages);

        public List<String> getPopulateParameters();

        public List<String> getDemoParameters();
}
