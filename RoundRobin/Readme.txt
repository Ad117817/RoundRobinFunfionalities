RoundRobin Load Balancer

The RoundRobin Load Balancer is a Java application that demonstrates a simple load balancing algorithm using the Round-Robin method. It is built with Spring Boot and designed to distribute HTTP requests among multiple workers based on their weights.

Table of Contents
1.Getting Started
2.Configuration
3.API Endpoints
4.Worker Statistics
5.Usage

Getting Started

To run the RoundRobin Load Balancer on your local machine, follow these steps:

1. Download this zip file and extract it in your local machine
2. Ensure you have Java and Maven installed.

3. Open the project in your preferred Java IDE.

3. Run the RoundRobinApplication class, which is the main entry point of the application.

4. The application will start, and you can access it at http://localhost:8080/api/v1.

Configuration

The application reads its configuration from a properties file located at C://Desktop//RoundRobin/text1.txt. You can customize the following configuration parameters in this file:

NumberOfWorkers: The number of worker instances to create.
RequestPoolSize: The size of the request pool to be processed.
StatsDirectory: The directory where worker statistics are stored.
AverageDelay: The average delay in seconds for request processing.
FailurePercentage: The percentage of failed requests.
WorkerWeight: The weight assigned to each worker for load balancing.
Ensure that you modify these values in the text1.txt file according to your requirements before running the application.

API Endpoints
The application exposes the following API endpoints:

/api/v1/hello: Simulates an HTTP request with a random delay and response status, demonstrating load balancing among workers.
/api/v1/worker/stats: Provides statistics about worker performance, success, and failure rates.

Worker Statistics
The application keeps track of worker statistics, including:

Total successful requests per worker.
Total failed requests per worker.
Total requests processed per worker.
Average request processing time per worker.
These statistics are updated in real-time and can be accessed via the /api/v1/worker/stats endpoint.

Usage
Start the application as described in the Getting Started section.

Access the API endpoints using a tool like Postman or a web browser.

Observe the load balancing among workers and the worker statistics.

Analyze the worker statistics to understand the performance of each worker in handling requests.