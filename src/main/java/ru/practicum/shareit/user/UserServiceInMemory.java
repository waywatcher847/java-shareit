//package ru.practicum.shareit.user;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Service;
//import ru.practicum.shareit.exception.ConflictException;
//import ru.practicum.shareit.exception.ValidationException;
//import ru.practicum.shareit.user.model.User;
//
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//
//@Service
//@Slf4j
//@Qualifier("UserServiceInMemory")
//public class UserServiceInMemory implements UserService {
//    private final UserStorage userStorage;
//
//    @Autowired
//    public UserServiceInMemory(@Qualifier("UserStorageInMemory") UserStorage userStorage) {
//        this.userStorage = userStorage;
//    }
//
//    @Override
//    public UserDto create(UserDtoNew userDtoNew) {
//        log.info("UserServiceInMemory->create start");
//        Optional<User> user = userStorage
//                .getAll()
//                .stream()
//                .filter(Objects::nonNull)
//                .filter(user1 -> user1.getEmail()
//                        .equals(userDtoNew.getEmail()))
//                .findFirst();
//        if (user.isPresent()) {
//            throw new ConflictException("Email already exist: " + userDtoNew.getEmail());
//        }
//        User userResult = UserMapper.mapToUser(userDtoNew);
//        User userCreate = userStorage.create(userResult);
//        log.info("UserServiceInMemory->create end");
//        return UserMapper.mapToUserDto(userCreate);
//    }
//
//    @Override
//    public void delete(Integer userId) {
//        log.info("UserServiceInMemory->delete start");
//        userStorage.delete(userId);
//        log.info("UserServiceInMemory->delete end");
//    }
//
//    @Override
//    public List<UserDto> getAll() {
//        log.info("UserServiceInMemory->getAll start");
//        List<UserDto> userDtoList = userStorage.getAll().stream()
//                .filter(Objects::nonNull)
//                .map(UserMapper::mapToUserDto)
//                .toList();
//        log.info("UserServiceInMemory->getAll end");
//        return userDtoList;
//    }
//
//    @Override
//    public UserDto getById(Integer userId) {
//        log.info("UserServiceInMemory->getById start");
//        User user = userStorage.getById(userId);
//        log.info("UserServiceInMemory->getById end");
//        return UserMapper.mapToUserDto(user);
//    }
//
//    @Override
//    public UserDto update(UserDtoUpdate updateUserDto, Integer userId) {
//        log.info("UserServiceInMemory->update start");
//        Optional<User> user = userStorage.getAll().stream()
//                .filter(Objects::nonNull)
//                .filter(user1 -> user1.getEmail()
//                        .equals(updateUserDto.getEmail()))
//                .findFirst();
//        if (user.isPresent()) {
//            throw new ConflictException("Email already exist: " + updateUserDto.getEmail());
//        }
//        User userResult = userStorage.getById(userId);
//        if (userResult == null) {
//            throw new ValidationException("User not found: " + updateUserDto.getId());
//        }
//        User userUpdate = UserMapper.updateUserField(userResult, updateUserDto);
//        log.info("UserServiceInMemory-update end");
//        return UserMapper.mapToUserDto(userUpdate);
//    }
//}
