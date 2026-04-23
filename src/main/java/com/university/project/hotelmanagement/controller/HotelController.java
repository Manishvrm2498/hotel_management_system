package com.university.project.hotelmanagement.controller;

import com.university.project.hotelmanagement.dto.HotelRequestDTO;
import com.university.project.hotelmanagement.dto.HotelResponseDTO;
import com.university.project.hotelmanagement.dto.RoomResponseDTO;
import com.university.project.hotelmanagement.entity.Hotel;
import com.university.project.hotelmanagement.services.HotelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam String state,
            @RequestParam String district) {
        return ResponseEntity.ok(hotelService.searchByLocation(state, district));
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

    @GetMapping("/search/state")
    public ResponseEntity<List<HotelResponseDTO>> getHotelByState(@RequestParam String state) {
        return ResponseEntity.ok(hotelService.searchHotelByState(state));
    }

    @GetMapping("/search/district")
    public ResponseEntity<List<HotelResponseDTO>> getHotelByDistrict(@RequestParam String district) {
        return ResponseEntity.ok(hotelService.searchHotelByDistrict(district));
    }

    @GetMapping("/searchBy")
    public ResponseEntity<List<HotelResponseDTO>> searchHotels(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Double rating) {

        return ResponseEntity.ok(hotelService.searchHotels(name, district, rating));
    }
}