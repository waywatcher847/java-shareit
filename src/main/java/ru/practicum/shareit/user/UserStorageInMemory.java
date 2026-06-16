//package ru.practicum.shareit.user;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@Component
//@Qualifier("UserStorageInMemory")
//public class UserStorageInMemory implements UserStorage {
//    private final Map<Integer, User> users = new HashMap<>();
//
//    @Override
//    public List<User> getAll() {
//        log.info("UserStorageInMemory->getAll start");
//        List<User> userList = users.values().stream().toList();
//        log.info("UserStorageInMemory->getAll end");
//        return userList;
//    }
//
//    @Override
//    public User getById(Integer userId) {
//        log.info("UserStorageInMemory->getById start");
//        User user = users.get(userId);
//        log.info("UserStorageInMemory->getById end");
//        return user;
//    }
//
//    @Override
//    public User create(User user) {
//        log.info("UserStorageInMemory->create start");
//        user.setId(counter());
//        users.put(user.getId(), user);
//        log.info("UserStorageInMemory->create end");
//        return user;
//    }
//
//    @Override
//    public User update(User user) {
//        log.info("UserStorageInMemory->update start");
//        users.put(user.getId(), user);
//        log.info("UserStorageInMemory->update end");
//        return user;
//    }
//
//    @Override
//    public void delete(Integer userId) {
//        log.info("UserStorageInMemory->delete start");
//        users.remove(userId);
//        log.info("UserStorageInMemory->delete end");
//    }
//
//    private Integer counter() {
//        int currentMaxId = users.keySet().stream()
//                .mapToInt(id -> id)
//                .max().orElse(0);
//
//        return ++currentMaxId;
//    }
//}
