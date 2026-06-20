import java.time.LocalDate;

public class CheckDate {
    public static void main(String[] args) {
        LocalDate date = LocalDate.of(2026, 6, 18);
        System.out.println("Day of Week: " + date.getDayOfWeek());
    }
}