package org.abr.memearenabot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a contest entry in the system
 */
@Entity
@Table(name = "contest_entries", indexes = {@Index(name = "idx_entry_contest", columnList = "contest_id"),
        @Index(name = "idx_entry_user", columnList = "user_id")})
@Data
@NoArgsConstructor
@ToString(exclude = {"contest", "user", "meme", "votes"})
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContestEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    @NotNull(message = "Contest cannot be null")
    @NonNull
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meme_id", nullable = false)
    @NotNull(message = "Meme cannot be null")
    @NonNull
    private Meme meme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User cannot be null")
    @NonNull
    private User user;

    @NotNull(message = "Submission date cannot be null")
    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer votesCount = 0;

    @Column
    private Integer rank;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ContestVote> votes = new ArrayList<>();

    /**
     * Set submission date before persisting
     */
    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }

    /**
     * Add vote to the entry
     */
    public void addVote(ContestVote vote) {
        votes.add(vote);
        vote.setEntry(this);
        incrementVotes();
    }

    /**
     * Increment votes count
     */
    public void incrementVotes() {
        this.votesCount = (this.votesCount == null) ? 1 : this.votesCount + 1;
    }
}