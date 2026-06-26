package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByOwner_Id(Integer userId);

    @Query("select i " +
            "from Item as i " +
            "join fetch i.owner " +
            "where i.id = :itemId")
    Optional<Item> findByWithOwner(@Param("itemId") Integer itemId);

    @Query("select i " +
            "from Item AS i " +
            "join fetch i.owner " +
            "where (lower(i.name) like lower(concat('%', :text, '%')) " +
            "OR lower(i.description) like lower(concat('%', :text, '%'))) " +
            "and i.available = true")
    List<Item> findByText(@Param("text") String text);

    List<Item> findByRequestId(Integer requestId);

}
