package com.jd.journalq.monitor;

public class PartitionLeaderAckMonitorInfo extends PartitionAckMonitorInfo {
    public PartitionLeaderAckMonitorInfo(){

    }
    public PartitionLeaderAckMonitorInfo(PartitionAckMonitorInfo partitionAckMonitorInfo, boolean leader){
        super(partitionAckMonitorInfo.getPartition(),partitionAckMonitorInfo.getIndex(),
                partitionAckMonitorInfo.getLeftIndex(),partitionAckMonitorInfo.getRightIndex());
        this.leader=leader;
    }
    private boolean leader;

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }
}