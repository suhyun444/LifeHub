package com.suhyun444.lifehub.aircon;

import com.suhyun444.lifehub.aircon.Aircon;

import jakarta.persistence.LockModeType;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AirconRepository extends JpaRepository<Aircon, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Aircon a WHERE a.id = :id")
    Optional<Aircon> findByIdForUpdate(@Param("id") Long id);
}