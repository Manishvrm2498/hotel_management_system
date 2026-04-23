package com.university.project.hotelmanagement.controller;

import com.university.project.hotelmanagement.dto.*;
import com.university.project.hotelmanagement.services.BookingService;
import com.university.project.hotelmanagement.services.HotelService;
import com.university.project.hotelmanagement.services.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminHotelController {


    private final HotelService hotelService;
    private final BookingService bookingService;
    private final RoomService roomService;

    public AdminHotelController(HotelService hotelService, BookingService bookingService, RoomService roomService) {
        this.hotelService = hotelService;
        this.bookingService = bookingService;
        this.roomService = roomService;
    }

    @PostMapping("/add")
    public ResponseEntity<HotelResponseDTO> addHotel(@RequestBody HotelRequestDTO hotelDto) {
        return ResponseEntity.ok(hotelService.saveHotel(hotelDto));
    }

    @PutMapping("/update/{hotelId}")
    public ResponseEntity<HotelResponseDTO> updateHotel(@Valid @RequestBody HotelRequestDTO hotelDto, @PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.updateHotel(hotelId, hotelDto));
    }

    @PostMapping("/add-room")
    public ResponseEntity<RoomResponseDTO> addRoom(@RequestBody RoomRequest roomDto) {
        return ResponseEntity.ok(roomService.saveRoom(roomDto));
    }

    @PatchMapping("/update/{roomId}")
    public ResponseEntity<RoomResponseDTO> createRoom(@PathVariable Long roomId,@Valid @RequestBody RoomRequest roomRequest) {
        return ResponseEntity.ok(roomService.updateRoom(roomId,roomRequest));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> getBookingDetails(@PathVariable Long bookingId) {
        BookingResponseDTO response = bookingService.getBookingByIdForAdmin(bookingId);
        return ResponseEntity.ok(response);
    }
}