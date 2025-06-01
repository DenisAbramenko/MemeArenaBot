package org.abr.memearenabot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a meme contest in the system
 */
@Entity
@Table(name = "contests", indexes = {@Index(name = "idx_contest_status", columnList = "status"), @Index(name =
        "idx_contest_dates", columnList = "startDate, endDate")})
@Data
@NoArgsConstructor
@ToString(exclude = {"entries", "createdBy"})
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Contest title cannot be empty")
    @Column(nullable = false)
    @NonNull
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @NonNull
    private User createdBy;

    @NotNull(message = "Creation date cannot be null")
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @NotNull(message = "Start date cannot be null")
    @Column(nullable = false)
    @NonNull
    private LocalDateTime startDate;

    @NotNull(message = "End date cannot be null")
    @Column(nullable = false)
    @NonNull
    private LocalDateTime endDate;

    @NotNull(message = "Contest status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContestStatus status;

    @Size(max = 1000, message = "Prize description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String prizeDescription;

    @Min(value = 1, message = "Maximum entries per user must be at least 1")
    @Column(nullable = false)
    @Builder.Default
    private Integer maxEntriesPerUser = 3;

    @OneToMany(mappedBy = "contest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ContestEntry> entries = new ArrayList<>();

    /**
     * Set creation date and status before persisting
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            if (LocalDateTime.now().isBefore(startDate)) {
                status = ContestStatus.SCHEDULED;
            } else if (LocalDateTime.now().isBefore(endDate)) {
                status = ContestStatus.ACTIVE;
            } else {
                status = ContestStatus.COMPLETED;
            }
        }
    }

    /**
     * Add entry to the contest
     */
    public void addEntry(ContestEntry entry) {
        entries.add(entry);
        entry.setContest(this);
    }

    /**
     * Check if contest is active
     */
    public boolean isActive() {
        return status == ContestStatus.ACTIVE;
    }

    /**
     * Check if voting is active
     */
    public boolean isVotingActive() {
        return status == ContestStatus.VOTING;
    }

    /**
     * Status of a contest
     */
    public enum ContestStatus {
        DRAFT, SCHEDULED, ACTIVE, VOTING, COMPLETED, CANCELLED
    }
} 