package com.LoadBalancer.RoundRobin;

import java.util.HashMap;
import java.util.Map;

public class WorkerStatistics {
    // Maps to store statistics for each worker
    private Map<String, Integer> successRequest; 
    private Map<String, Integer> failedRequest;  
    private Map<String, Integer> totalRequest;   
    private Map<String, Double> avgRequestTime;  

    // Constructor to initialize the maps when a WorkerStatistics object is created
    public WorkerStatistics() {
        this.successRequest = new HashMap<>();
        this.failedRequest = new HashMap<>();
        this.totalRequest = new HashMap<>();
        this.avgRequestTime = new HashMap<>();
    }

    // Getter methods to retrieve the statistics maps
    
    public Map<String, Integer> getSuccessRequest() {
        return successRequest;
    }

    public Map<String, Integer> getFailedRequest() {
        return failedRequest;
    }

    public Map<String, Integer> getTotalRequest() {
        return totalRequest;
    }

    public Map<String, Double> getAvgRequestTime() {
        return avgRequestTime;
    }
}
