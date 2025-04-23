package ru.xgodness.util;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import ru.xgodness.exception.ValidationException;
import ru.xgodness.endpoint.user.model.Role;

import java.util.ArrayList;
import java.util.List;

@Log
@Component
public class ValidationUtils {

    public static FieldValidator fieldValidator() {
        return new FieldValidator();
    }

    public static class FieldValidator {
        List<String> errorMessages;

        public FieldValidator() {
            this.errorMessages = new ArrayList<>();
        }

        public void validate() {
            if (!errorMessages.isEmpty()) {
                log.warning(errorMessages.toString());
                throw new ValidationException(errorMessages);
            }
        }

        public FieldValidator username(String username) {
            if (username == null)
                errorMessages.add("Имя пользователя не может быть пустым");
            else {
                if (!username.matches("[a-zA-Z0-9]+"))
                    errorMessages.add("Имя пользователя может состоять из букв латинского алфавита и цифр");
                if (username.length() > 64)
                    errorMessages.add("Имя пользователя не может быть длиннее 64 символов");
            }
            return this;
        }

        public FieldValidator password(String password) {
            if (password == null)
                errorMessages.add("Пароль не может быть пустым");
            else if (password.length() < 8)
                errorMessages.add("Пароль не может быть короче 8 символов");
            return this;
        }

        public FieldValidator role(String role) {
            if (role == null || role.isEmpty())
                errorMessages.add("Роль пользователя не может быть пустой");
            else try {
                Role.valueOfIgnoreCase(role);
            } catch (Exception ex) {
                errorMessages.add("Роль пользователя может принимать значения 'клиент' (client) или 'специалист' (specialist)");
            }
            return this;
        }

        // TODO: remove
        @Deprecated
        public FieldValidator stringNotEmpty(String field, String fieldName) {
            if (field == null || field.isEmpty())
                errorMessages.add("Поле %s не может быть пустым".formatted(fieldName));
            return this;
        }
    }

    // TODO: adapt it somehow or remove
//    public static class PersistenceValidator {
//        private final UserRepository userRepository;
//
//        @Autowired
//        public PersistenceValidator(UserRepository userRepository) {
//            this.userRepository = userRepository;
//        }
//
//        public void userShouldExist(String username) {
//            if (!userRepository.existsByUsername(username))
//                throw new UsernameNotFoundException(username);
//        }
//
//        public void userShouldNotExists(String username) {
//            if (userRepository.existsByUsername(username))
//                throw new UsernameAlreadyTakenException(username);
//        }
//    }
}
