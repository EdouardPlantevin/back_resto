package com.edouard.back_resto.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date createdAt;
    private String name;
    private String address;
    private String description;
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_id")
    private Squad squad;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Rating> ratings = new ArrayList<>();
}
