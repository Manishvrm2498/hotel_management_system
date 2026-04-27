package com.university.project.hotelmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingRequestDTO {

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Check-in date is mandatory")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is mandatory")
    @Future(message = "Check-out date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    @Min(value = 1, message = "At least one guest is required")
    @Max(value = 10, message = "Maximum 10 guests allowed per booking")
    private int totalGuests;
}