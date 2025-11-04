package com.edouard.back_resto.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "users")
@ToString(exclude = "squads")
@EqualsAndHashCode(exclude = "squads")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String username;
    private String password;

    @CreatedDate
    private Date createdAt;
    private String role;

    @ManyToMany(mappedBy = "users")
    private Set<Squad> squads = new HashSet<>();

    public void addSquad(Squad squad) {
        this.squads.add(squad);
        squad.getUsers().add(this);
    }

    public void removeSquad(Squad squad) {
        this.squads.remove(squad);
        squad.getUsers().remove(this);
    }

}

