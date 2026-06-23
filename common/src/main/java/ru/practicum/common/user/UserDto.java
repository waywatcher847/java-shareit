package ru.practicum.common.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Integer id;
    @NotBlank(groups = Create.class, message = "Name is mandatory")
    private String name;

    @NotNull(groups = Create.class, message = "Email is mandatory")
    @NotEmpty(groups = Create.class, message = "Email is mandatory")
    @Email(groups = {Create.class, Edit.class}, message = "not a email")
    private String email;

    public interface Create {
    }

    public interface Edit {
    }
}
