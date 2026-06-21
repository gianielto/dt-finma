package com.dt_finma.dt_finma.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table (name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank (message = "El email es obligatorio")
    @Email (message = "El email debe tener un formato valido")
    @Column (nullable = false, unique = true)
    private String email;

    @NotBlank (message = "Password obligatorio")
    @Column (nullable = false )
    private String password;

    @Column (nullable = false)
    private String role = "USER";

    @Column (name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected  void onCreated(){
        this.createdAt = LocalDateTime.now();
    }

}
