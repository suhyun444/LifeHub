package com.suhyun444.lifehub.marker.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

import com.suhyun444.lifehub.card.Entity.User;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Marker
{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String color; 

   @JoinColumn(name = "userId")
    private User user;

    @OneToMany(mappedBy = "marker", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Link> links = new ArrayList<>();
}