package com.university.project.hotelmanagement.repository;

import com.university.project.hotelmanagement.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);


//    @Query("SELECT r FROM Room r JOIN r.hotel h WHERE h.state = :state AND h.district = :district")
//    List<Room> findAvailableRooms(@Param("state") String state, @Param("district") String district);

//    Optional<Room> findByIdAndHotelAdminUsername(Long roomId, String username);

    boolean existsByHotelIdAndType(Long hotelId,String type);
}