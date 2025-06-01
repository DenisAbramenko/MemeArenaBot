package org.abr.memearenabot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a user in the system
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_telegram_id", columnList = "telegramId", unique = true),
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@ToString(exclude = "memes")
@EqualsAndHashCode(of = {"id", "telegramId"})
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Telegram ID cannot be empty")
    @Column(nullable = false, unique = true)
    @NonNull
    private String telegramId;
    
    @Column
    private String username;
    
    @Column
    private String firstName;
    
    @Column
    private String lastName;
    
    @Column
    private String languageCode;
    
    @NotNull(message = "Creation date cannot be null")
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime lastActivity;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer totalMemes = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer totalLikes = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPremium = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isAdmin = false;
    
    @Column(nullable = true)
    private LocalDateTime premiumSince;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Meme> memes = new ArrayList<>();
    
    /**
     * Set creation and last activity date before persisting
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (lastActivity == null) {
            lastActivity = now;
        }
    }
    
    /**
     * Update last activity date before updating
     */
    @PreUpdate
    protected void onUpdate() {
        lastActivity = LocalDateTime.now();
    }
    
    /**
     * Update user activity
     */
    public void updateActivity() {
        lastActivity = LocalDateTime.now();
    }
    
    /**
     * Increment total memes count
     */
    public void incrementMemes() {
        this.totalMemes = (this.totalMemes == null) ? 1 : this.totalMemes + 1;
    }
    
    /**
     * Increment total likes count
     */
    public void incrementLikes() {
        this.totalLikes = (this.totalLikes == null) ? 1 : this.totalLikes + 1;
    }
    
    /**
     * Add meme to user's collection
     */
    public void addMeme(Meme meme) {
        memes.add(meme);
        meme.setUser(this);
        incrementMemes();
    }
} 