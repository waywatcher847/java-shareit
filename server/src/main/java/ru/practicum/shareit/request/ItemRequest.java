package ru.practicum.shareit.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "requests")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User requestor;

    @Column(name = "created")
    private LocalDateTime created;
}
