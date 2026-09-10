package com.jyotimoykashyap;

import com.jyotimoykashyap.cache.CacheDao;
import com.jyotimoykashyap.cache.InMemoryCache;
import com.jyotimoykashyap.dao.InMemoryTaskDao;
import com.jyotimoykashyap.dao.TaskDao;
import com.jyotimoykashyap.models.Task;
import com.jyotimoykashyap.repository.TaskRepository;
import com.jyotimoykashyap.service.TaskService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Starting Task Management System Demo ===\n");

        // 1. Initialize components (Set capacity = 2 to clearly test LRU eviction)
        CacheDao cacheDao = new InMemoryCache.Builder()
                .capacity(2)
                .build();
        TaskDao taskDao = InMemoryTaskDao.getInstance();
        TaskRepository repository = new TaskRepository(cacheDao, taskDao);

        // Initialize Notification System (Observer Pattern)
        com.jyotimoykashyap.notification.publisher.EventPublisher publisher =
                new com.jyotimoykashyap.notification.publisher.SimpleEventPublisher();
        publisher.addSubscriber(new com.jyotimoykashyap.notification.subscriber.EmailNotifier());

        TaskService taskService = new TaskService(repository, publisher);

        try {
            // 2. Create and Save Tasks
            System.out.println("--- 1. Creating and Saving Tasks ---");
            Task task1 = new Task("Setup Database", "Configure PostgreSQL connection", LocalDate.now().plusDays(2));
            Task task2 = new Task("Implement Auth", "Add JWT authentication", LocalDate.now().plusDays(5));
            Task task3 = new Task("Write Unit Tests", "Test cache and repository layers", LocalDate.now().plusDays(3));

            UUID id1 = taskService.saveTask(task1);
            System.out.println("Saved Task 1: " + id1);

            UUID id2 = taskService.saveTask(task2);
            System.out.println("Saved Task 2: " + id2);
            // Cache currently holds: [Task 2 (Head), Task 1 (Tail)]

            // 3. Fetch Task (Cache Hit)
            System.out.println("\n--- 2. Fetching Task 1 (Cache Hit & LRU Promotion) ---");
            Task fetchedTask1 = taskService.getTask(id1);
            System.out.println("Successfully fetched Task 1: " + fetchedTask1.getId());
            // Accessing Task 1 promotes it to head: [Task 1 (Head), Task 2 (Tail)]

            // 4. Test LRU Eviction by saving Task 3 (Capacity is 2)
            System.out.println("\n--- 3. Saving Task 3 (Triggers LRU Eviction of Task 2) ---");
            UUID id3 = taskService.saveTask(task3);
            System.out.println("Saved Task 3: " + id3);
            // Task 2 was at the tail, so it got evicted from cache!
            // Cache now holds: [Task 3 (Head), Task 1 (Tail)]

            // 5. Fetch Task 2 (Cache Miss -> Loaded from DB -> Added back to Cache)
            System.out.println("\n--- 4. Fetching Task 2 (Cache Miss -> DB Fetch) ---");
            Task fetchedTask2 = taskService.getTask(id2);
            System.out.println("Successfully fetched Task 2 from DB: " + fetchedTask2.getId());

            // 6. Update Task (Direct update & DTO update with Assignee Notification)
            System.out.println("\n--- 5. Updating Task 1 (Assignee Change & Email Notification) ---");
            com.jyotimoykashyap.models.User alice = new com.jyotimoykashyap.models.User("alice");
            com.jyotimoykashyap.dto.UpdateTaskRequest updateRequest = new com.jyotimoykashyap.dto.UpdateTaskRequest(
                    id1,
                    null,
                    "Updated description for PostgreSQL",
                    alice,
                    null,
                    null
            );
            taskService.updateTask(updateRequest);
            System.out.println("Updated Task 1 via UpdateTaskRequest successfully.");

            // 7. Get All Tasks
            System.out.println("\n--- 6. Listing All Tasks ---");
            List<Task> allTasks = taskService.getAllTasks();
            System.out.println("Total tasks in system: " + allTasks.size());
            for (Task t : allTasks) {
                System.out.println(" - Task ID: " + t.getId());
            }

            // 8. Delete Task
            System.out.println("\n--- 7. Deleting Task 3 ---");
            taskService.deleteTask(id3);
            System.out.println("Deleted Task 3.");

            // Verify Task 3 is gone
            try {
                taskService.getTask(id3);
            } catch (RuntimeException e) {
                System.out.println("Verified: Task 3 no longer exists (" + e.getMessage() + ")");
            }

            System.out.println("\n=== All Tests Completed Successfully! ===");

        } catch (Exception e) {
            System.err.println("Unexpected error during execution: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
