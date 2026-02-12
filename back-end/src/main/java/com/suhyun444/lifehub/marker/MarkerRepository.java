package com.suhyun444.lifehub.marker;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.suhyun444.lifehub.marker.Entity.Marker;

public interface MarkerRepository extends JpaRepository<Marker,Long>{
    List<Marker> findAllByUserIdOrderBySortOrderDesc(Long userId);
    @Query("SELECT COALESCE(MAX(m.sortOrder), 0) FROM Marker m WHERE m.user.id = :userId")
    Optional<Long> findMaxSortOrder(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Marker m SET m.sortOrder = m.sortOrder + 1 " +
           "WHERE m.user.id = :userId AND m.sortOrder >= :start AND m.sortOrder < :end")
    void shiftOrdersIncrement(@Param("userId") Long userId, @Param("start") Long start, @Param("end") Long end);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Marker m SET m.sortOrder = m.sortOrder - 1 " +
           "WHERE m.user.id = :userId AND m.sortOrder > :start AND m.sortOrder <= :end")
    void shiftOrdersDecrement(@Param("userId") Long userId, @Param("start") Long start, @Param("end") Long end);
}
