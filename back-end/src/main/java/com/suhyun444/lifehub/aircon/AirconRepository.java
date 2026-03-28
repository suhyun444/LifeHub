package com.suhyun444.lifehub.aircon;

import com.suhyun444.lifehub.aircon.Aircon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirconRepository extends JpaRepository<Aircon, Long> {
}