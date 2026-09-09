package com.pack.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "teams",
        indexes = {
                @Index(name = "idx_team_invite_token", columnList = "invite_token"),
                @Index(name = "idx_team_owner", columnList = "owner_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Team extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "owner_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_team_owner")
    )
    private User owner;

    @Column(name = "team_name", nullable = false, length = 100)
    private String teamName;

    @Column(name = "invite_token", unique = true, length = 100)
    private String inviteToken;

    /** Set via the dedicated Image upload API — not writable through TeamRequestDto. */
    @Column(name = "icon_url")
    private String iconUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}