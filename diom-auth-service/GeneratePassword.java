import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String encoded = encoder.encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt加密后: " + encoded);
        System.out.println("验证结果: " + encoder.matches(password, encoded));
    }
}

