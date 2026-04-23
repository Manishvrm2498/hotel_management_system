package com.university.project.hotelmanagement.repository;

import com.university.project.hotelmanagement.entity.Booking;
import com.university.project.hotelmanagement.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(UserEntity user);

    @Query("SELECT b FROM Booking b JOIN FETCH b.room r JOIN FETCH r.hotel h WHERE b.user.id = :userId")
    List<Booking> findAllBookingsByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.room r JOIN FETCH r.hotel h")
    List<Booking> findAllDetailedBookings();

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.room WHERE b.id = :id")
    Optional<Booking> findByBookingIdWithDetails(@Param("id") Long id);

    List<Booking> findAllByUserId(Long userId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId " +
            "AND (:checkIn < b.checkOutDate AND :checkOut > b.checkInDate)")
    boolean existsByRoomIdAndDateRange(Long roomId, LocalDate checkIn, LocalDate checkOut);
}