package com.suhyun444.lifehub.marker.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import com.suhyun444.lifehub.card.Entity.User;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "marker",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_sort_order", // 제약조건 이름
            columnNames = {"user_id", "sort_order"} // 유저ID와 순서의 조합은 유니크해야 함
        )
    }
)
public class Marker
{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String color; 
    
    @Column(name="sort_order")
    private Long sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @OneToMany(mappedBy = "marker", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Link> links = new ArrayList<>();

    public Marker(String title, String color, User user,Long sortOrder)
    {
        this.title = title;
        this.color = color;
        this.user = user;
        this.sortOrder = sortOrder;
        this.links = new ArrayList<>();
    }
}