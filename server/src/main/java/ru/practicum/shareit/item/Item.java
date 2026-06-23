package ru.practicum.shareit.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.comment.Comment;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "items")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {

    @Id
    @JsonProperty("id")
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @JsonProperty("name")
    @Column(name = "name")
    String name;

    @JsonProperty("description")
    @Column(name = "description")
    String description;

    @JsonProperty("available")
    @Column(name = "available")
    Boolean available;

    @JsonProperty("userId")
    @Column(name = "user_id")
    Integer userId;

    @JsonProperty("requestId")
    @Column(name = "request_id")
    Integer requestId;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private List<Comment> comments = new ArrayList<>();
}