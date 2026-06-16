//package ru.practicum.shareit.item;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Component;
//import ru.practicum.shareit.item.model.Item;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@Component
//@Qualifier("ItemStorageInMemory")
//public class ItemStorageInMemory implements ItemStorage {
//    private final Map<Integer, Item> items = new HashMap<>();
//
//    @Override
//    public List<Item> getAll() {
//        log.info("ItemStorageInMemory->getAll start");
//        List<Item> itemList = items.values().stream().toList();
//        log.info("ItemStorageInMemory->getAll end");
//        return itemList;
//    }
//
//    @Override
//    public Item getById(Integer itemId) {
//        log.info("ItemStorageInMemory->getById start");
//        Item item = items.get(itemId);
//        log.info("ItemStorageInMemory->getById end");
//        return item;
//    }
//
//    @Override
//    public Item create(Item item) {
//        log.info("ItemStorageInMemory->create start");
//        item.setId(counter());
//        items.put(item.getId(), item);
//        log.info("ItemStorageInMemory->create end");
//        return item;
//    }
//
//    @Override
//    public Item update(Item item) {
//        log.info("ItemStorageInMemory->update start");
//        items.put(item.getId(), item);
//        log.info("ItemStorageInMemory->update end");
//        return item;
//    }
//
//    @Override
//    public void delete(Integer itemId) {
//        log.info("ItemStorageInMemory->delete start");
//        items.remove(itemId);
//        log.info("ItemStorageInMemory->delete end");
//    }
//
//    private Integer counter() {
//        int currentMaxId = items.keySet()
//                .stream()
//                .mapToInt(id -> id)
//                .max()
//                .orElse(0);
//
//        return ++currentMaxId;
//    }
//}
