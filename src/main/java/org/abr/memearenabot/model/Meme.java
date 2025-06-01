package org.abr.memearenabot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a meme in the system
 */
@Entity
@Table(name = "memes", indexes = {@Index(name = "idx_meme_user_id", columnList = "userId"), @Index(name =
        "idx_meme_in_contest", columnList = "inContest"), @Index(name = "idx_meme_created_at", columnList =
        "createdAt")})
@Data
@NoArgsConstructor
@ToString(exclude = "user")
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Column(nullable = false)
    @NonNull
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_entity_id")
    private User user;

    @NotNull(message = "Creation date cannot be null")
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @NotNull(message = "Meme type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemeType type;

    @Column
    private String templateId;

    @Column(nullable = false)
    @Builder.Default
    private Integer likes = 0;

    @Column
    private String nftUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean inContest = false;

    @Builder.Default
    private boolean publishedToFeed = false;

    private LocalDateTime publishedAt;

    private LocalDateTime nftCreatedAt;

    /**
     * Constructor for AI-generated memes
     */
    public Meme(@NonNull String imageUrl, String description, @NonNull String userId) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.userId = userId;
        this.type = MemeType.AI_GENERATED;
    }

    /**
     * Constructor for AI-generated memes with User entity
     */
    public Meme(@NonNull String imageUrl, String description, @NonNull User user) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.user = user;
        this.userId = user.getTelegramId();
        this.type = MemeType.AI_GENERATED;
    }

    /**
     * Constructor for template-based memes
     */
    public Meme(@NonNull String imageUrl, @NonNull String templateId, @NonNull String userId, boolean isTemplate) {
        this.imageUrl = imageUrl;
        this.templateId = templateId;
        this.userId = userId;
        this.type = MemeType.TEMPLATE_BASED;
    }

    /**
     * Constructor for template-based memes with User entity
     */
    public Meme(@NonNull String imageUrl, @NonNull String templateId, @NonNull User user, boolean isTemplate) {
        this.imageUrl = imageUrl;
        this.templateId = templateId;
        this.user = user;
        this.userId = user.getTelegramId();
        this.type = MemeType.TEMPLATE_BASED;
    }

    /**
     * Set creation date before persisting
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
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
        AI_GENERATED, TEMPLATE_BASED, VOICE_GENERATED
    }
} 