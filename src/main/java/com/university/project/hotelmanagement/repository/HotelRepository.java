package com.university.project.hotelmanagement.repository;

import com.university.project.hotelmanagement.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByStateAndDistrict(String state, String district);

    List<Hotel> findByNameContainingIgnoreCase(String name);

    boolean existsByNameAndAddress(String trim, String trim1);

    List<Hotel> findByDistrictContainingIgnoreCase(String district);

    List<Hotel> findByStateContainingIgnoreCase(String state);

    List<Hotel> findByNameAndDistrictAndRatingContainingIgnoreCase(String name, String district, Double rating);

    List<Hotel> findAll();
}