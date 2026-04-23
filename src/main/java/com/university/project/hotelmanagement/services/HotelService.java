package com.university.project.hotelmanagement.services;

import com.university.project.hotelmanagement.dto.HotelRequestDTO;
import com.university.project.hotelmanagement.dto.HotelResponseDTO;
import com.university.project.hotelmanagement.dto.RoomRequest;
import com.university.project.hotelmanagement.dto.RoomResponseDTO;
import com.university.project.hotelmanagement.entity.Hotel;
import com.university.project.hotelmanagement.entity.Room;
import com.university.project.hotelmanagement.entity.UserEntity;
import com.university.project.hotelmanagement.exception.DuplicateResourceException;
import com.university.project.hotelmanagement.exception.ResourceNotFoundException;
import com.university.project.hotelmanagement.repository.HotelRepository;
import com.university.project.hotelmanagement.repository.RoomRepository;
import com.university.project.hotelmanagement.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public HotelService(HotelRepository hotelRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }


    public HotelResponseDTO saveHotel(HotelRequestDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        UserEntity admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean exists = hotelRepository.existsByNameAndAddress(
                request.getName().trim(),
                request.getAddress().trim()
        );
        if (exists) {
            throw new DuplicateResourceException("Hotel already exists");
        }
        Hotel hotel = new Hotel();
        hotel.setName(request.getName().trim());
        hotel.setState(request.getState().trim());
        hotel.setDistrict(request.getDistrict().trim());
        hotel.setAddress(request.getAddress().trim());
        hotel.setContactNumber(request.getContactNumber());
        hotel.setRating(request.getRating());
        hotel.setDescription(request.getDescription());
        hotel.setAdmin(admin);

        return mapToHotelDTO(hotelRepository.save(hotel));
    }

    public List<HotelResponseDTO> searchByLocation(String state, String district) {
        return hotelRepository.findByStateAndDistrict(state, district).stream()
                .map(this::mapToHotelDTO).collect(Collectors.toList());
    }

    public List<HotelResponseDTO> searchByName(String name) {
        return hotelRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToHotelDTO)
                .collect(Collectors.toList());
    }

    public List<HotelResponseDTO> searchHotelByID(Long id) {
        return hotelRepository.findById(id)
                .stream()
                .map(this::mapToHotelDTO)
                .collect(Collectors.toList());

    }

    public List<HotelResponseDTO> searchHotelByState(String state) {

        return hotelRepository.findByStateContainingIgnoreCase(state)
                .stream()
                .map(this::mapToHotelDTO)
                .collect(Collectors.toList());
    }


    public List<HotelResponseDTO> searchHotelByDistrict(String district) {

        return hotelRepository.findByDistrictContainingIgnoreCase(district)
                .stream()
                .map(this::mapToHotelDTO)
                .collect(Collectors.toList());
    }

    public List<HotelResponseDTO> searchHotels(String name, String district, Double rating) {

        List<Hotel> hotels = hotelRepository.findAll();

        return hotels.stream()
                .filter(hotel -> name == null ||
                        hotel.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(hotel -> district == null ||
                        hotel.getDistrict().toLowerCase().contains(district.toLowerCase()))
                .filter(hotel -> rating == null ||
                        hotel.getRating() >= rating)
                .map(hotel -> HotelResponseDTO.builder()
                        .id(hotel.getId())
                        .name(hotel.getName())
                        .district(hotel.getDistrict())
                        .address(hotel.getAddress())
                        .rating(hotel.getRating())
                        .build())
                .toList();
    }


    public List<RoomResponseDTO> getRoomsByHotel(Long hotelId) {
        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(room -> {
                    RoomResponseDTO dto = new RoomResponseDTO();
                    dto.setId(room.getId());
                    dto.setType(room.getType());
                    dto.setPrice(room.getPrice());
                    dto.setTotalRooms(room.getTotalRooms());
                    dto.setHotelName(room.getHotel().getName());
                    dto.setHotelId(room.getHotel().getId());
                    return dto;
                }).collect(Collectors.toList());
    }

    private HotelResponseDTO mapToHotelDTO(Hotel hotel) {
        HotelResponseDTO dto = new HotelResponseDTO();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setState(hotel.getState());
        dto.setDistrict(hotel.getDistrict());
        dto.setAddress(hotel.getAddress());
        dto.setContactNumber(hotel.getContactNumber());
        dto.setRating(hotel.getRating());
        dto.setDescription(hotel.getDescription());
        return dto;
    }
    private RoomResponseDTO mapToRoomDTO(Room r) {
        RoomResponseDTO dto = new RoomResponseDTO();
        dto.setId(r.getId());
        dto.setType(r.getType());
        dto.setPrice(r.getPrice());
        dto.setTotalRooms(r.getTotalRooms());
        dto.setHotelName(r.getHotel().getName());
        return dto;
    }


    public HotelResponseDTO updateHotel(Long id, HotelRequestDTO request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if (!hotel.getAdmin().getEmail().equals(email)) {
            throw new AccessDeniedException("You are not allowed to modify this hotel");
        }

        hotel.setName(request.getName().trim());
        hotel.setState(request.getState().trim());
        hotel.setDistrict(request.getDistrict().trim());
        hotel.setAddress(request.getAddress().trim());
        hotel.setContactNumber(request.getContactNumber());
        hotel.setRating(request.getRating());
        hotel.setDescription(request.getDescription());

        return mapToHotelDTO(hotelRepository.save(hotel));
    }



}