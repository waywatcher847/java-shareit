package ru.practicum.shareit.item.comment;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "text")
    String text;

    @Column(name = "item_id")
    Integer itemId;

    @Column(name = "user_id")
    Integer userId;

    @Column(name = "created")
    Instant created;
}