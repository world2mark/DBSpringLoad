package com.auracoda.dbspringload.Workloads;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkloadThreadRunner {

    final String RunningStatusKey;
    final String ThreadRunning = "yes";
    final String ThreadStopped = "no";

    final String SuccessCountKey;
    final String FailCountKey;
    final String ActualDurationKey;
    final String ActualTargetCountKey;

    ConcurrentHashMap<String, String> sharedMap;

    final BusinessLogicInterface MyWorkload;

    public WorkloadThreadRunner(BusinessLogicInterface incomingWorkload,
            ConcurrentHashMap<String, String> sharedMap, int threadIndex) {
        this.MyWorkload = incomingWorkload;
        final String workloadName = MyWorkload.getClass().getSimpleName();
        RunningStatusKey = "Running:" + workloadName + threadIndex;
        SuccessCountKey = "SuccessCount:" + workloadName + threadIndex;
        FailCountKey = "FailCount:" + workloadName + threadIndex;

        ActualDurationKey = "ActualDuration:" + workloadName + threadIndex;
        ActualTargetCountKey = "ActualTargetCount:" + workloadName + threadIndex;

        this.sharedMap = sharedMap;

        sharedMap.put(RunningStatusKey, ThreadStopped);
        UpdateCountMap(0, 0);
    }

    private void UpdateCountMap(long successCount, long failCount) {
        sharedMap.put(SuccessCountKey, String.valueOf(successCount));
        sharedMap.put(FailCountKey, String.valueOf(failCount));
    };

    public boolean WorkloadRunning() {
        if (sharedMap.get(RunningStatusKey).equals(ThreadRunning)) {
            // System.out.println("still running");
        } else {
            // System.out.println("Not running");
        }
        return sharedMap.get(RunningStatusKey).equals(ThreadRunning);
    };

    public long GetSuccessCount() {
        return Long.parseLong(sharedMap.get(SuccessCountKey));
    }

    public long GetFailCount() {
        return Long.parseLong(sharedMap.get(FailCountKey));
    }

    public void RunWorkload(
            long incomingDuration,
            long incomingTargetCount,
            Map<String, String> MyWorkloadParams) throws IllegalStateException {
        sharedMap.put(ActualDurationKey, String.valueOf(incomingDuration));
        sharedMap.put(ActualTargetCountKey, String.valueOf(incomingTargetCount));

        if (WorkloadRunning()) {
            throw new IllegalStateException("Thread still running, cannot create another with the same name.");
        }

        sharedMap.put(RunningStatusKey, ThreadRunning);
        UpdateCountMap(0, 0);

        Thread.startVirtualThread(() -> {
            long duration = Long.parseLong(sharedMap.get(ActualDurationKey));
            long targetcount = Long.parseLong(sharedMap.get(ActualTargetCountKey));

            long SuccessCountValue = 0;
            long FailCountValue = 0;
            try {

                MyWorkload.PrepareConnectionsAndStatements();

                final long endTime = System.currentTimeMillis() + (duration * 1000);

                long rightNow = System.currentTimeMillis();

                while (WorkloadRunning() &&
                        (targetcount < 0 || (SuccessCountValue + FailCountValue) < targetcount) &&
                        (duration < 0 || endTime >= rightNow)) {

                    if (MyWorkload.RunBusinessLogic(MyWorkloadParams)) {
                        SuccessCountValue++;
                    } else {
                        FailCountValue++;
                    }

                    // System.out.print(SuccessCountValue + FailCountValue);

                    UpdateCountMap(SuccessCountValue, FailCountValue);

                    rightNow = System.currentTimeMillis();
                }

            } catch (Exception sqlE) {
                System.out.println(sqlE.getLocalizedMessage());
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                StopWorkload();
            } finally {
                UpdateCountMap(SuccessCountValue, FailCountValue);
                sharedMap.put(RunningStatusKey, ThreadStopped);
                try {
                    MyWorkload.TerminateConnections();
                } catch (SQLException sqlE) {
                    System.out.println(sqlE.getLocalizedMessage());
                }
            }

        });
    }

    public void StopWorkload() {
        sharedMap.put(RunningStatusKey, ThreadStopped);
    }

}
