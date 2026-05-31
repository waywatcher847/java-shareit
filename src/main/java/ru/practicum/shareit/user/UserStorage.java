package ru.practicum.shareit.user;

import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserStorage {

    List<User> getAll();

    User getById(Integer userId);

    User create(User user);

    User update(User user);

    void delete(Integer userId);
}
