package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Integer> {

    @Query("SELECT ir FROM ItemRequest ir WHERE ir.userId = :userId ORDER BY ir.created DESC")
    List<ItemRequest> findByRequesterIdOrderByCreatedDesc(@Param("userId") Integer userId);

    @Query("SELECT ir FROM ItemRequest ir WHERE ir.userId <> :userId ORDER BY ir.created DESC")
    List<ItemRequest> findByRequesterIdNotOrderByCreatedDesc(@Param("userId") Integer userId, Pageable pageable);
}