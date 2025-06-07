package org.abr.memearenabot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a meme in the system
 */
@Entity
@Table(name = "memes", indexes = {@Index(name = "idx_meme_user_id", columnList = "userId"), @Index(name =
        "idx_meme_in_contest", columnList = "in_contest"), @Index(name = "idx_meme_created_at", columnList =
        "createdAt")})
@Data
@ToString(exclude = "user")
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Meme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Image URL cannot be empty")
    @Column(nullable = false)
    @NonNull
    private String imageUrl;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "telegram_id", insertable = false, updatable = false)
    private User user;

    @NotNull(message = "Creation date cannot be null")
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @NotNull(message = "Meme type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemeType type;

    @Column(nullable = false)
    @Builder.Default
    private Integer likes = 0;

    @Column(name = "in_contest", nullable = false)
    @Builder.Default
    private boolean inContest = false;

    @Column(name = "published_to_feed", nullable = false)
    @Builder.Default
    private boolean publishedToFeed = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * Default constructor
     */
    public Meme() {
    }

    /**
     * Constructor with basic info
     */
    public Meme(@NonNull String imageUrl, String description, @NonNull String userId) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.userId = userId;
        this.type = MemeType.AI_GENERATED;
        this.inContest = false;
        this.likes = 0;
        this.publishedToFeed = false;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor with User entity
     */
    public Meme(@NonNull String imageUrl, String description, @NonNull User user) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.user = user;
        this.userId = user.getTelegramId();
        this.type = MemeType.AI_GENERATED;
        this.inContest = false;
        this.likes = 0;
        this.publishedToFeed = false;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Set creation date before persisting
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        // Явная установка значения для likes, даже если оно уже установлено
        likes = (likes != null) ? likes : 0;
    }

    /**
     * Increment likes count
     */
    public void incrementLikes() {
        this.likes = (this.likes == null) ? 1 : this.likes + 1;
        if (user != null) {
            user.incrementLikes();
        }
    }

    /**
     * Types of memes in the system
     */
    public enum MemeType {
        AI_GENERATED
    }
} 