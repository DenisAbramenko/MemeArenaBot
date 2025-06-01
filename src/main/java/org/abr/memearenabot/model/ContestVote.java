package org.abr.memearenabot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entity representing a vote in a contest
 */
@Entity
@Table(name = "contest_votes", 
    indexes = {
        @Index(name = "idx_vote_entry", columnList = "entry_id"),
        @Index(name = "idx_vote_user", columnList = "user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_entry_vote", columnNames = {"user_id", "entry_id"})
    }
)
@Data
@NoArgsConstructor
@ToString(exclude = {"entry", "user"})
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContestVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    @NotNull(message = "Contest entry cannot be null")
    @NonNull
    private ContestEntry entry;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User cannot be null")
    @NonNull
    private User user;
    
    @NotNull(message = "Vote date cannot be null")
    @Column(nullable = false)
    private LocalDateTime votedAt;
    
    /**
     * Set vote date before persisting
     */
    @PrePersist
    protected void onCreate() {
        if (votedAt == null) {
            votedAt = LocalDateTime.now();
        }
    }
} 