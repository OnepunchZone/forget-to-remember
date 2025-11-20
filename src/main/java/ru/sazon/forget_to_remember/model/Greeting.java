package ru.sazon.forget_to_remember.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "greetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "greeting-owner-graph",
                attributeNodes = {
                        @NamedAttributeNode("owner")
                }
        ),
        @NamedEntityGraph(
                name = "greeting-with-likes-graph",
                attributeNodes = {
                        @NamedAttributeNode("owner"),
                        @NamedAttributeNode(value = "likedBy", subgraph = "likedByDetails")
                },
                subgraphs = {
                        @NamedSubgraph(
                                name = "likedByDetails",
                                attributeNodes = {
                                        @NamedAttributeNode("username")  // Детали юзера в лайках
                                }
                        )
                }
        )
})
public class Greeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Поле text должно быть заполнено")
    @Size(min = 1, max = 1000, message = "Поле должно содержать от 1 до 1000 символов")
    private String text;

    @Size(max = 500, message = "Поле media URL должно содержать не более 500 символов")
    private String mediaUrl;

    @Column(nullable = false)
    private boolean isPublic = false;

    @Column(nullable = false)
    private int likesCount = 0;

    @ManyToMany
    @JoinTable(
            name = "greeting_likes",
            joinColumns = @JoinColumn(name = "greeting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedBy = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    public void addLike(User user) {
        if (likedBy.add(user)) {
            likesCount++;
        }
    }
}
