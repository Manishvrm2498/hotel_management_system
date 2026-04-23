package com.university.project.hotelmanagement.controller;

import com.university.project.hotelmanagement.dto.BookingRequestDTO;
import com.university.project.hotelmanagement.dto.BookingResponseDTO;
import com.university.project.hotelmanagement.dto.UserBookingDetailsDTO;
import com.university.project.hotelmanagement.entity.UserEntity;
import com.university.project.hotelmanagement.services.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/confirm")
    public ResponseEntity<BookingResponseDTO> bookRoom(@Valid @RequestBody BookingRequestDTO request) {
        BookingResponseDTO response = bookingService.createBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponseDTO>> getUserBookings() {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByUser();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/my-bookings-details")
    public ResponseEntity<List<UserBookingDetailsDTO>> getMyBookings() {
        List<UserBookingDetailsDTO> myBookings = bookingService.getUserFullDetails();
        return ResponseEntity.ok(myBookings);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelMyBooking(@PathVariable Long id) {
        String message = bookingService.cancelBooking(id);
        return ResponseEntity.ok(message);
    }
}