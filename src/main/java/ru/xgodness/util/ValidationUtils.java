package ru.xgodness.util;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import ru.xgodness.endpoint.user.model.Role;
import ru.xgodness.util.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                if (username.length() > 32)
                    errorMessages.add("Имя пользователя не может быть длиннее 32 символов");
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

        public FieldValidator clientUsernameNotEmpty(String clientUsername) {
            if (clientUsername == null || clientUsername.isBlank())
                errorMessages.add("Не указано имя пользователя клиента");
            return this;
        }

        public FieldValidator answerUpdates(Map<Integer, Integer> answerUpdates, int questionCount, int answerCount) {
            if (answerUpdates == null) {
                errorMessages.add("Ответы на вопросы не указаны");
                return this;
            }

            Integer key, value;
            for (var entry : answerUpdates.entrySet()) {
                key = entry.getKey();
                value = entry.getValue();
                if (key == null || value == null)
                    continue;

                if (key <= 0 || key > questionCount)
                    errorMessages.add("Неверный номер вопроса: %d".formatted(key));

                if (value <= 0 || value > answerCount)
                    errorMessages.add("Неверный номер варианта ответа: %d".formatted(value));
            }

            return this;
        }
    }

}
