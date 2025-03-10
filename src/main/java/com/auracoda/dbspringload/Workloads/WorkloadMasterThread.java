package com.auracoda.dbspringload.Workloads;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkloadMasterThread {

    protected ConcurrentHashMap<String, String> sharedMap = new ConcurrentHashMap<>();

    private final List<WorkloadThreadRunner> MyThreads = new ArrayList<>();

    public void AddThread(BusinessLogicInterface workloadInterface) {
        final WorkloadThreadRunner newRunner = new WorkloadThreadRunner(workloadInterface, sharedMap,
                MyThreads.size());
        MyThreads.add(newRunner);
    };

    public void StartWorkload(
            long duration,
            long targetcount,
            Map<String, String> MyWorkloadParams) {

        Thread.startVirtualThread(() -> {

            final long testStart = System.currentTimeMillis();

            for (WorkloadThreadRunner myThread : MyThreads) {
                myThread.RunWorkload(
                        duration,
                        targetcount,
                        MyWorkloadParams);
            }

            boolean WorkloadRunning = true;
            while (WorkloadRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }

                long SuccessfulRuns = 0;
                long FailedRuns = 0;

                WorkloadRunning = false;
                for (WorkloadThreadRunner myThread : MyThreads) {
                    SuccessfulRuns += myThread.GetSuccessCount();
                    FailedRuns += myThread.GetFailCount();
                    if (myThread.WorkloadRunning()) {
                        WorkloadRunning = true;
                    }
                }

                System.out.println("Total executions: " + (SuccessfulRuns + FailedRuns) + ", Successful runs ("
                        + SuccessfulRuns + "), Failed runs (" + FailedRuns + ")");
            }

            final long testEnd = System.currentTimeMillis();

            System.out.println("All threads completed, time taken: " + ((testEnd - testStart) / 1000) + " seconds");

        });

    }

    public void StopWorkload() {
        for (WorkloadThreadRunner myThread : MyThreads) {
            myThread.StopWorkload();
        }

        System.out.println("Workload terminating...");
    }

}
