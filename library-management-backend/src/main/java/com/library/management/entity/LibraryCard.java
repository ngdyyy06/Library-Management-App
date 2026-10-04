package com.library.management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "library_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LibraryCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="card_number", nullable = false, unique = true)
    private String cardNumber;

    @OneToOne
    @JoinColumn(name = "reader_id", nullable = false, unique = true)
    private Reader reader;

    @Column(name = "issued_at", nullable = false)
    private LocalDate issuedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDate expiredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    public enum CardStatus {
        ACTIVE,
        INACTIVE
    }
}