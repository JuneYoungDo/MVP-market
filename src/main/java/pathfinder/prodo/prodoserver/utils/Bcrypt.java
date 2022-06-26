package pathfinder.prodo.prodoserver.utils;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Bcrypt {

    public String encrypt(String pwd) {
        return BCrypt.hashpw(pwd, BCrypt.gensalt());
    }

    public boolean isMatch(String pwd, String hashed) {
        return BCrypt.checkpw(pwd,hashed);
    }
}