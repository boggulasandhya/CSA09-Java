package assignment3.core.exam.controller;

import assignment3.core.exam.ExamSubmission;
import assignment3.core.exam.exceptions.EvaluationServiceException;
import assignment3.core.exam.exceptions.ExamException;
import assignment3.core.exam.exceptions.ExamValidationException;
import assignment3.core.exam.service.ExamService;
import assignment3.core.util.ConsoleUtil;

/**
 * Controller Tier: Acts as the gateway / REST-style controller facade.
 * Catches service exceptions, inspects cause chains (getCause()), and maps to user-safe responses.
 */
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    /**
     * Handles incoming exam submission requests from the portal UI.
     *
     * @param submission Incoming exam submission payload
     * @return boolean indicating whether the submission was accepted
     */
    public boolean handleSubmissionRequest(ExamSubmission submission) {
        System.out.println(ConsoleUtil.MAGENTA + "[Controller Tier] Received HTTP POST /api/v1/exams/submit for Submission ID: " 
                + (submission != null ? submission.getSubmissionId() : "NULL") + ConsoleUtil.RESET);

        try {
            if (submission == null) {
                throw new ExamValidationException("payload", "Request body payload cannot be null.");
            }

            // Propagate request to Service Tier
            examService.processSubmission(submission);

            ConsoleUtil.printSuccess("[Controller Tier] 201 CREATED: Exam submission accepted and recorded.");
            return true;

        } catch (ExamValidationException valEx) {
            // Client error (400 Bad Request)
            ConsoleUtil.printWarning("[Controller Tier] 400 BAD REQUEST: Validation Constraint Failed");
            System.out.println("   Field:   " + valEx.getFieldName());
            System.out.println("   Details: " + valEx.getMessage());
            return false;

        } catch (EvaluationServiceException servEx) {
            // Server error with chained cause (500 Internal Server Error)
            ConsoleUtil.printError("[Controller Tier] 500 INTERNAL SERVER ERROR: Exam Processing Pipeline Failed");
            System.out.println("   High-Level Context: " + servEx.getMessage());

            // DEEP EXCEPTION CHAINING INSPECTION:
            inspectExceptionChain(servEx);
            return false;

        } catch (ExamException generalExamEx) {
            ConsoleUtil.printError("[Controller Tier] Generic Exam Error: " + generalExamEx.getMessage());
            inspectExceptionChain(generalExamEx);
            return false;
        }
    }

    /**
     * Recursively traverses and displays the cause chain using getCause().
     */
    public static void inspectExceptionChain(Throwable topLevelException) {
        System.out.println(ConsoleUtil.YELLOW + "   === [Layered Exception Propagation & Chain Analysis] ===" + ConsoleUtil.RESET);
        
        Throwable current = topLevelException;
        int level = 1;

        while (current != null) {
            System.out.printf("   [Layer %d] %s%n", level, current.getClass().getName());
            System.out.printf("            Message: %s%n", current.getMessage());
            
            // Check if there is a cause underneath
            Throwable cause = current.getCause();
            if (cause != null) {
                System.out.printf(ConsoleUtil.CYAN + "            |---> Caused by: %s%n" + ConsoleUtil.RESET, cause.getClass().getSimpleName());
            } else {
                System.out.println(ConsoleUtil.GREEN + "            `---> [ROOT CAUSE IDENTIFIED]" + ConsoleUtil.RESET);
            }
            
            current = cause;
            level++;
        }
        System.out.println(ConsoleUtil.YELLOW + "   ==========================================================" + ConsoleUtil.RESET);
    }
}
