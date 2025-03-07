package com.auracoda.dbspringload.Workloads;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auracoda.dbspringload.Workloads.TransactionRetries.TransactionRetries;
import com.auracoda.dbspringload.Workloads.ContentionDemo.ContentionDemo;
import com.auracoda.dbspringload.Workloads.DeclarativeTriggers.DeclarativeTriggers;
import com.auracoda.dbspringload.Workloads.InsertReadDemo.InsertReadDemo;
import com.auracoda.dbspringload.Workloads.PaymentsDemo.PaymentsDemo;

@Service
@RestController
public class WorkloadLibraryEndpoints {

    List<WorkloadService> MyWorkloads = new ArrayList<>();

    @Autowired
    DataSource myDataSource;

    WorkloadLibraryEndpoints() {
        MyWorkloads.add(new InsertReadDemo());
        MyWorkloads.add(new PaymentsDemo());
        MyWorkloads.add(new TransactionRetries());
        MyWorkloads.add(new DeclarativeTriggers());
        MyWorkloads.add(new ContentionDemo());
    };

    @GetMapping("create-tables")
    public String CreateTables(@RequestParam(required = true) String workloadName) {

        final AJAXMessages myMessages = new AJAXMessages();

        try {
            GetWorkloadByName(workloadName).CreateTables(myDataSource, myMessages);
        } catch (Exception myEx) {
            myMessages.AddError(myEx.getLocalizedMessage());
        }

        return myMessages.GenerateResponseJSON();
    };

    @GetMapping("populate-tables")
    public String PopulateTables(
            @RequestParam(required = true) String workloadName,
            @RequestParam(required = false) Integer rowCount,
            @RequestParam Map<String, String> workloadParams) {

        final AJAXMessages myMessages = new AJAXMessages();

        try {
            long actualRowCount = 1;

            if (rowCount != null) {
                actualRowCount = rowCount.longValue();
            }
            if (actualRowCount <= 0 || actualRowCount > 100000) {
                throw new IllegalArgumentException("The rowCount paramter value must be between 1 and 100000");
            }
            GetWorkloadByName(workloadName).PopulateTables(actualRowCount, workloadParams, myDataSource, myMessages);
        } catch (Exception myEx) {
            myMessages.AddError(myEx.getLocalizedMessage());
        }

        return myMessages.GenerateResponseJSON();
    };

    @GetMapping("drop-tables")
    public String DropTables(
            @RequestParam(required = true) String workloadName) {

        final AJAXMessages myMessages = new AJAXMessages();

        try {
            GetWorkloadByName(workloadName).DropTables(myDataSource, myMessages);
        } catch (Exception myEx) {
            myMessages.AddError(myEx.getLocalizedMessage());
        }

        return myMessages.GenerateResponseJSON();
    };

    @GetMapping("run-workload")
    public String RunWorkload(
            @RequestParam(required = true) String workloadName,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) Integer threads,
            @RequestParam Map<String, String> workloadParams) throws Exception {

        final AJAXMessages myMessages = new AJAXMessages();

        try {
            long actualDuration = -1;
            if (duration != null) {
                actualDuration = duration.longValue();
            }
            long actualCount = -1;
            if (count != null) {
                actualCount = count.longValue();
            }

            if (actualDuration <= 0 && actualCount <= 0) {
                throw new IllegalArgumentException("Both the DURATION and COUNT must not be unbounded");
            }

            long actualThreads = 1;
            if (threads != null) {
                actualThreads = threads.longValue();
            }

            if (actualThreads < 1 || actualThreads > 256) {
                throw new IllegalArgumentException("The THREAD count must be between 1 and 256");
            }

            GetWorkloadByName(workloadName).RunWorkload(
                    actualDuration,
                    actualCount,
                    actualThreads,
                    workloadParams,
                    myDataSource,
                    myMessages);
        } catch (Exception myEx) {
            myMessages.AddError(myEx.getLocalizedMessage());
        }

        return myMessages.GenerateResponseJSON();
    }

    @GetMapping("stop-workload")
    public String StopWorkload(
            @RequestParam(required = true) String workloadName) {

        final AJAXMessages myMessages = new AJAXMessages();

        try {
            GetWorkloadByName(workloadName).StopWorkload(myMessages);
        } catch (Exception myEx) {
            myMessages.AddError(myEx.getLocalizedMessage());
        }

        return myMessages.GenerateResponseJSON();
    }

    private WorkloadService GetWorkloadByName(String workloadName) throws IllegalArgumentException {
        for (final WorkloadService workload : MyWorkloads) {
            if (workload.getClass().getSimpleName().equals(workloadName)) {
                return workload;
            }
        }
        throw new IllegalArgumentException("The workload \"" + workloadName + "\" does not exist");
    };

    public List<WorkloadService> ListWorkloads() {
        return MyWorkloads;
    }

}
