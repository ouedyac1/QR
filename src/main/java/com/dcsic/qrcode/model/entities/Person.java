package com.dcsic.qrcode.model.entities;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "person")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identifiant unique pour le QR code (non modifiable)
    @Column(unique = true, nullable = false, updatable = false)
    private String uniqueCode;

    // Informations personnelles
    private String nom;
    private String prenom;
    private String email;
    private String titre;


    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "lieu_naissance")
    private String lieuNaissance;

    private String region;
    private String pays;
    private String nationalite;

    @Column(name = "groupe_sanguin")
    private String groupeSanguin;

    private String religion;

    // Informations militaires/professionnelles
    @Column(name = "date_service")
    private LocalDate dateService;

    @Column(name = "arme_service")
    private String armeService;

    private String grade;

    @Column(name = "date_promotion")
    private LocalDate datePromotion;

    @Column(name = "email_etudiant")
    private String emailEtudiant;

    private String adresse;

    @Column(name = "derniere_fonction")
    private String derniereFonction;

    @Column(name = "situation_matrimoniale")
    private String situationMatrimoniale;

    // Métadonnées
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "verified")
    private boolean verified = true; // Par défaut, les diplômes sont valides

    @PrePersist
    public void generateUniqueCode() {
        if (this.uniqueCode == null) {
            this.uniqueCode = "DIP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

    // Méthode pour obtenir le nom complet
    public String getFullName() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

//----------------------------------------------------------------------
//    relation entre personne et school on reviendra a ça

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;
}