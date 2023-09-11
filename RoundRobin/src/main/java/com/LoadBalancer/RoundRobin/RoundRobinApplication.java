package com.LoadBalancer.RoundRobin;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
public class RoundRobinApplication {

    private static Random random = new Random();

    // Atomic counters for total requests, successful requests, and failed requests
    private static AtomicInteger totalRequests = new AtomicInteger(0);
    private static AtomicInteger totalSuccessRequests = new AtomicInteger(0);
    private static AtomicInteger totalFailedRequests = new AtomicInteger(0);

    private static String statsDirectory = "C://Desktop//RoundRobin/text2.txt";

    private static List<Worker> workers = new ArrayList<>();
    private static LoadBalancer loadBalancer;

    public static void main(String[] args) {
        // Configuration manager to load settings
        ConfigurationManager configManager = new ConfigurationManager();
        configManager.loadConfiguration();

        // Create worker instances based on configuration
        for (int i = 0; i < configManager.getNumWorkers(); i++) {
            workers.add(new Worker("Worker" + (i + 1), configManager.getWorkerWeight()));
        }

        loadBalancer = new LoadBalancer(workers);

        SpringApplication.run(RoundRobinApplication.class, args);

        // Simulate sending requests to workers
        for (int i = 0; i < configManager.getRequestPoolSize(); i++) {
            Worker selectedWorker = loadBalancer.getNextWorker();
            System.out.println("Request " + i + " sent to " + selectedWorker.getName());
        }

        // Print worker load statistics
        for (Worker worker : workers) {
            System.out.println(worker.getName() + " Load: " + worker.getCurrentLoad());
        }
        System.out.println("Configuration manager finished.");
    }

    @RestController
    @RequestMapping("/api/v1")
    static class ApiController {

        // Simulate a delay for request processing
        @GetMapping("/hello")
        public ResponseEntity<ResponseMessage> hello() {
            // Configuration values for delay and failure rate
            double averageDelay = config.AVERAGE_DELAY;
            double actualDelay = averageDelay + (Math.random() * 0.2 - 0.1);
            int delayMillis = (int) (actualDelay * 1000); // Convert seconds to milliseconds

            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            double requestTime = actualDelay + (delayMillis / 1000.0);
            int rand = random.nextInt(100);
            boolean success = rand > config.FAILURE_PERCENT;
            totalRequests.incrementAndGet();

            int workerId = Thread.currentThread().hashCode();
            updateWorkerStatistics(workerId, success, requestTime);

            if (success) {
                totalSuccessRequests.incrementAndGet();
                return ResponseEntity.ok(new ResponseMessage("hello-world"));
            } else {
                totalFailedRequests.incrementAndGet();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage("request-failed"));
            }
        }

        // Response message class
        static class ResponseMessage {
            private String message;

            public ResponseMessage(String message) {
                this.message = message;
            }

            public String getMessage() {
                return message;
            }

            public void setMessage(String message) {
                this.message = message;
            }
        }

        // Get worker statistics
        @GetMapping("/worker/stats")
        public WorkerStatistics workerStats() {
            WorkerStatistics workerStats = new WorkerStatistics();

            for (Worker worker : workers) {
                String workerName = worker.getName();
                int total = worker.getCurrentLoad();
                int success = totalSuccessRequests.get();
                int failed = totalFailedRequests.get();
                double avgRequestTime = (double) total / totalRequests.get();

                workerStats.getSuccessRequest().put(workerName, success);
                workerStats.getFailedRequest().put(workerName, failed);
                workerStats.getTotalRequest().put(workerName, total);
                workerStats.getAvgRequestTime().put(workerName, avgRequestTime);
            }

            // Add total statistics
            workerStats.getSuccessRequest().put("total", totalSuccessRequests.get());
            workerStats.getFailedRequest().put("total", totalFailedRequests.get());
            workerStats.getTotalRequest().put("total", totalRequests.get());
            workerStats.getAvgRequestTime().put("total", calculateTotalAverageRequestTime());

            return workerStats;
        }

        // Calculate total average request time
        private double calculateTotalAverageRequestTime() {
            double totalAverage = 0.0;
            int totalRequestCount = totalRequests.get();

            if (totalRequestCount > 0) {
                for (Worker worker : workers) {
                    int workerRequestCount = worker.getCurrentLoad();
                    if (workerRequestCount > 0) {
                        totalAverage += (double) workerRequestCount / totalRequestCount * worker.getWeight();
                    }
                }
            }

            return totalAverage;
        }

        // Update worker statistics and write request details
        private void updateWorkerStatistics(int workerId, boolean success, double delay) {
            String workerStatsFile = "C://Desktop//RoundRobin/text2.txt";

            synchronized (this) {
                try (FileWriter writer = new FileWriter(workerStatsFile, true)) {
                    writer.write("Request: " + (success ? "SUCCESS" : "FAILURE") + "\n");
                    writer.write("Delay: " + delay + " milliseconds\n");
                    writer.write("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Worker worker = getWorkerById(workerId);
            if (worker != null) {
                totalRequests.incrementAndGet();
                if (success) {
                    totalSuccessRequests.incrementAndGet();
                    worker.incrementSuccessCount();
                } else {
                    totalFailedRequests.incrementAndGet();
                    worker.incrementFailureCount();
                }
            }
        }

        private Worker getWorkerById(int workerId) {
            for (Worker worker : workers) {
                if (worker.hashCode() == workerId) {
                    return worker;
                }
            }
            return null;
        }
    }

    // Configuration settings
    static class config {
        public static final double AVERAGE_DELAY = 0.5;
        public static final int FAILURE_PERCENT = 12;
    }

    // Worker class representing a server
    static class Worker {
        private String name;
        private int weight;
        private int currentLoad;
        private int successCount;
        private int failureCount;

        public Worker(String name, int weight) {
            this.name = name;
            this.weight = weight;
            this.currentLoad = 0;
            this.successCount = 0;
            this.failureCount = 0;
        }

        public void incrementLoad() {
            currentLoad++;
        }

        public void incrementSuccessCount() {
            successCount++;
        }

        public void incrementFailureCount() {
            failureCount++;
        }

        public String getName() {
            return name;
        }

        public int getWeight() {
            return weight;
        }

        public int getCurrentLoad() {
            return currentLoad;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }
    }

    // Load balancer class
    static class LoadBalancer {
        private List<Worker> workers;
        private int currentIndex;

        public LoadBalancer(List<Worker> workers) {
            this.workers = new ArrayList<>(workers);
            this.currentIndex = 0;
        }

        // Weighted round-robin logic to select the next worker
        public Worker getNextWorker() {
            Worker nextWorker = workers.get(currentIndex);
            nextWorker.incrementLoad();
            currentIndex = (currentIndex + 1) % workers.size();

            return nextWorker;
        }
    }

    // Configuration manager to load settings from a file
    static class ConfigurationManager {
        private int numWorkers;
        private int requestPoolSize;
        private double averageDelay;
        private int failurePercentage;
        private int workerWeight;

        public void loadConfiguration() {
            try (InputStream input = new FileInputStream("C://Desktop//RoundRobin/text1.txt")) {
                Properties properties = new Properties();
                properties.load(input);

                numWorkers = Integer.parseInt(properties.getProperty("NumberOfWorkers"));
                requestPoolSize = Integer.parseInt(properties.getProperty("RequestPoolSize"));
                statsDirectory = properties.getProperty("StatsDirectory");
                averageDelay = Float.parseFloat(properties.getProperty("AverageDelay"));
                failurePercentage = Integer.parseInt(properties.getProperty("FailurePercentage"));
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error loading configuration: " + e.getMessage());
                e.printStackTrace();
            }
        }

        public int getNumWorkers() {
            return numWorkers;
        }

        public int getRequestPoolSize() {
            return requestPoolSize;
        }

        public double getAverageDelay() {
            return averageDelay;
        }

        public int getFailurePecentage() {
            return failurePercentage;
        }

        public int getWorkerWeight() {
            return workerWeight;
        }
    }
}
