package com.mpp.rental.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BankAccount Entity - Stores banking information for users
 * One-to-One relationship with User
 */
@Entity
@Table(name = "bank_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_account_id")
    private Long bankAccountId;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "bank_acc_number", nullable = false, length = 50)
    private String bankAccNumber;

    /**
     * One-to-One relationship with User
     * @JoinColumn: Creates foreign key column in bank_account table
     * nullable = false: Bank account must belong to a user
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Helper method to set bidirectional relationship
     */
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            user.setBankAccount(this);
        }
    }
}