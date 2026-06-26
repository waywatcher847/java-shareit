package ru.practicum.shareit.item.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    @Query("""
        select c from Comment c
        join fetch c.item i
        join fetch c.author a
        where i.id = :itemId
        order by c.created desc
        """)
    List<Comment> getCommentByItemId(@Param("itemId") Integer itemId);

    @Query("""
        select c from Comment c
        join fetch c.item i
        join fetch c.author a
        where i.id in (:itemsId)
        order by i.id, c.created desc
        """)
    List<Comment> getCommentByAuthorIdAndItemId(@Param("itemsId") List<Integer> itemsId);
}
