package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findAllByUserId(Integer userId);

    @Query("SELECT i FROM Item i WHERE i.requestId = :requestId")
    List<Item> findByRequestId(@Param("requestId") Integer requestId);

    @Query("SELECT i FROM Item i " +
            "WHERE (UPPER(i.name) LIKE UPPER(CONCAT('%', :text, '%')) " +
            "OR UPPER(i.description) LIKE UPPER(CONCAT('%', :text, '%'))) " +
            "AND i.available = true")
    List<Item> searchItem(@Param("text") String text);

}