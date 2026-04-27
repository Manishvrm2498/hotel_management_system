package com.university.project.hotelmanagement.services;

import com.university.project.hotelmanagement.dto.RoomRequest;
import com.university.project.hotelmanagement.dto.RoomResponseDTO;
import com.university.project.hotelmanagement.entity.Hotel;
import com.university.project.hotelmanagement.entity.Room;
import com.university.project.hotelmanagement.exception.DuplicateResourceException;
import com.university.project.hotelmanagement.exception.ResourceNotFoundException;
import com.university.project.hotelmanagement.exception.UnauthorizedAccessException;
import com.university.project.hotelmanagement.repository.HotelRepository;
import com.university.project.hotelmanagement.repository.RoomRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {


    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    public RoomResponseDTO getRoomById(Long roomId) {
        SecurityContextHolder.getContext().getAuthentication();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

        return mapToResponseDTO(room);
    }


    public RoomResponseDTO saveRoom(RoomRequest roomDto) {

        Hotel hotel = hotelRepository.findById(roomDto.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        boolean exists = roomRepository.existsByHotelIdAndType(
                roomDto.getHotelId(),
                roomDto.getType());

        if (exists) {
            throw new DuplicateResourceException("Room type already exists for this hotel");
        }
        Room room = new Room();
        room.setHotel(hotel);
        room.setType(roomDto.getType());
        room.setPrice(roomDto.getPrice());
        room.setTotalRooms(roomDto.getTotalRooms());
        room.setAvailable(true);
       return mapToResponseDTO(roomRepository.save(room));
    }

    @Transactional
    public RoomResponseDTO updateRoom(Long roomId, RoomRequest roomDto) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        String adminEmail = room.getHotel().getAdmin().getEmail();

        if (adminEmail == null || !adminEmail.equals(currentUsername)) {
            throw new UnauthorizedAccessException("You are not allowed to update this room.");
        }
        if (roomDto.getType() != null) {
            room.setType(roomDto.getType());
        }
        if (roomDto.getPrice() != null) {
            room.setPrice(roomDto.getPrice());
        }
        if (roomDto.getTotalRooms() != null) {
            room.setTotalRooms(roomDto.getTotalRooms());
        }
        room.setAvailable(room.getTotalRooms() > 0);
        Room updatedRoom = roomRepository.save(room);

        return mapToResponseDTO(updatedRoom);
    }


//    public void deleteRoom(Long roomId, UserDetails userDetails) {
//        Room room = roomRepository.findById(roomId)
//                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
//
//        // SECURITY: Ownership check
//        if (!room.getHotel().getAdmin().getEmail().equals(userDetails.getUsername())) {
//            throw new UnauthorizedAccessException("Unauthorized to delete this room.");
//        }
//
//        roomRepository.delete(room);
//    }

    public RoomResponseDTO mapToResponseDTO(Room room) {

        return RoomResponseDTO.builder()
                .id(room.getId())
                .type(room.getType())
                .price(room.getPrice())
                .totalRooms(room.getTotalRooms())
                .hotelId(room.getHotel() != null ? room.getHotel().getId() : null)
                .hotelName(room.getHotel() != null ? room.getHotel().getName() : null)
                .isAvailable(room.isAvailable())
                .build();
    }
}