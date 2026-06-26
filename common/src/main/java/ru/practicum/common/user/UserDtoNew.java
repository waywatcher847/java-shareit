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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDtoNew {
    private Integer id;
    @NotBlank(groups = UserDtoNew.Create.class, message = "Name is mandatory")
    private String name;

    @NotNull(groups = UserDtoNew.Create.class, message = "Email is mandatory")
    @NotEmpty(groups = UserDtoNew.Create.class, message = "Email is mandatory")
    @Email(groups = {UserDtoNew.Create.class, UserDtoNew.Edit.class}, message = "not a email")
    private String email;

    public interface Create {
    }

    public interface Edit {
    }
}