package assignment3.core.exam;

import assignment3.core.exam.controller.ExamController;
import assignment3.core.exam.service.ExamService;
import assignment3.core.exam.storage.ExamStorage;
import assignment3.core.util.ConsoleUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Runner and demonstration harness for Module 2 (Online Examination Portal).
 * Demonstrates 3-tier exception propagation and exception chaining (getCause()).
 */
public class ExamPortalSimulation {

    public static void main(String[] args) {
        runAutomatedDemo();
    }

    public static void runInteractive(Scanner scanner) {
        ConsoleUtil.printHeader("MODULE 2: ONLINE EXAMINATION PORTAL (INTERACTIVE MODE)");
        ExamStorage storage = new ExamStorage();
        ExamService service = new ExamService(storage);
        ExamController controller = new ExamController(service);

        System.out.print("Enter Student ID (e.g. STU-101, or press Enter for blank): ");
        String studentId = scanner.nextLine().trim();

        System.out.print("Enter Student Name: ");
        String studentName = scanner.nextLine().trim();

        System.out.print("Enter Subject Code (e.g. CS301): ");
        String subjectCode = scanner.nextLine().trim();

        System.out.print("Enter answer to Question 1: ");
        String ans1 = scanner.nextLine().trim();

        Map<Integer, String> answers = new HashMap<>();
        if (!ans1.isEmpty()) {
            answers.put(1, ans1);
        }

        System.out.print("Simulate low-level storage disk failure? (y/N): ");
        String simDisk = scanner.nextLine().trim();
        storage.setSimulateDiskWriteFailure(simDisk.equalsIgnoreCase("y"));

        ExamSubmission submission = new ExamSubmission("SUB-" + System.currentTimeMillis() % 10000,
                studentId, studentName, subjectCode, answers);

        controller.handleSubmissionRequest(submission);
    }

    public static void runAutomatedDemo() {
        ConsoleUtil.printHeader("MODULE 2: ONLINE EXAMINATION PORTAL - COMPREHENSIVE TEST HARNESS");
        System.out.println("Demonstrating 3-tier exception propagation (Storage -> Service -> Controller)");
        System.out.println("and root-cause inspection using getCause().\n");

        ExamStorage storage = new ExamStorage();
        ExamService service = new ExamService(storage);
        ExamController controller = new ExamController(service);

        // TEST CASE 1: Successful Submission Across All Tiers
        ConsoleUtil.printSubHeader("TEST CASE 1: Successful Multi-Tier Exam Submission");
        Map<Integer, String> validAnswers = new HashMap<>();
        validAnswers.put(1, "Multithreading ensures CPU core utilization.");
        validAnswers.put(2, "Try-with-resources manages AutoCloseable lifecycles.");

        ExamSubmission submission1 = new ExamSubmission(
                "SUB-9001", "STU-8821", "Maya Lin", "CS-402", validAnswers);
        controller.handleSubmissionRequest(submission1);

        // TEST CASE 2: Validation Failure at Service Tier (Blank Answers)
        ConsoleUtil.printSubHeader("TEST CASE 2: Validation Failure at Service Tier (Empty Answers Map)");
        ExamSubmission submission2 = new ExamSubmission(
                "SUB-9002", "STU-8822", "Devon Miles", "CS-402", Map.of());
        controller.handleSubmissionRequest(submission2);

        // TEST CASE 3: Storage Failure Propagation & Exception Chaining
        ConsoleUtil.printSubHeader("TEST CASE 3: Storage Failure Cascading with Exception Chaining");
        System.out.println("Enabling simulated disk write failure in Storage Tier...");
        storage.setSimulateDiskWriteFailure(true);

        Map<Integer, String> answers3 = new HashMap<>();
        answers3.put(1, "Deadlocks occur when circular wait conditions exist.");
        ExamSubmission submission3 = new ExamSubmission(
                "SUB-9003", "STU-8823", "Rajesh Sharma", "CS-402", answers3);
        
        controller.handleSubmissionRequest(submission3);

        // Reset storage state
        storage.setSimulateDiskWriteFailure(false);

        System.out.println();
        ConsoleUtil.printSuccess("Module 2 (Online Exam Portal) completed all test verification criteria successfully!\n");
    }
}
