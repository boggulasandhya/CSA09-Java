package assignment3.core.results;

import java.util.Map;

/**
 * Domain entity representing an evaluated student academic record.
 */
public class StudentScore {
    private final String rollNumber;
    private final String studentName;
    private final Map<String, Double> subjectMarks;
    private final double gpa;
    private final String letterGrade;

    public StudentScore(String rollNumber, String studentName, Map<String, Double> subjectMarks) {
        this.rollNumber = rollNumber;
        this.studentName = studentName;
        this.subjectMarks = subjectMarks;
        this.gpa = calculateGpa(subjectMarks);
        this.letterGrade = calculateLetterGrade(this.gpa);
    }

    private static double calculateGpa(Map<String, Double> marks) {
        if (marks == null || marks.isEmpty()) return 0.0;
        double sum = 0.0;
        for (double m : marks.values()) {
            sum += m;
        }
        return sum / marks.size();
    }

    private static String calculateLetterGrade(double avg) {
        if (avg >= 90.0) return "A";
        if (avg >= 80.0) return "B";
        if (avg >= 70.0) return "C";
        if (avg >= 60.0) return "D";
        return "F";
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public Map<String, Double> getSubjectMarks() {
        return subjectMarks;
    }

    public double getGpa() {
        return gpa;
    }

    public String getLetterGrade() {
        return letterGrade;
    }
}
