import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CheckHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt0W.Om";
        System.out.println("Matches 'password': " + encoder.matches("password", hash));
        System.out.println("Matches '123456': " + encoder.matches("123456", hash));
        System.out.println("Matches 'enseignant3': " + encoder.matches("enseignant3", hash));
        System.out.println("Matches 'enseignant': " + encoder.matches("enseignant", hash));
        System.out.println("Matches 'admin': " + encoder.matches("admin", hash));
    }
}
