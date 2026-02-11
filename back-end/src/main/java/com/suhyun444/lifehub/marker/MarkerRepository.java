package com.suhyun444.lifehub.marker;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.suhyun444.lifehub.marker.Entity.Marker;

public interface MarkerRepository extends JpaRepository<Marker,Long>{
    List<Marker> findByUserIdOrderByIdDesc(Long userId);
}
