package com.university.project.hotelmanagement.controller;

import com.university.project.hotelmanagement.dto.HotelRequestDTO;
import com.university.project.hotelmanagement.dto.HotelResponseDTO;
import com.university.project.hotelmanagement.dto.RoomResponseDTO;
import com.university.project.hotelmanagement.entity.Hotel;
import com.university.project.hotelmanagement.repository.HotelRepository;
import com.university.project.hotelmanagement.services.HotelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<HotelResponseDTO>> searchByLocation(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(hotelService.searchByLocation(state, district,name));
    }

    @GetMapping("/availability")
    public ResponseEntity<?> checkAvailability(
            @RequestParam String district,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut) {

        return ResponseEntity.ok(
                hotelService.checkAvailability(district, checkIn, checkOut)
        );
    }

    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<RoomResponseDTO>> getRooms(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getRoomsByHotel(hotelId));
    }

    @GetMapping("/find")
    public ResponseEntity<List<HotelResponseDTO>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(hotelService.searchByName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<HotelResponseDTO>> getHotel(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.searchHotelByID(id));
    }

    @GetMapping("/searchBy")
    public ResponseEntity<List<HotelResponseDTO>> searchHotels(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Double rating) {

        return ResponseEntity.ok(hotelService.searchHotels(name, district, rating));
    }
}