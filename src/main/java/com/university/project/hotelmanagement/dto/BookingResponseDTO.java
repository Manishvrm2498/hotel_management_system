package com.university.project.hotelmanagement.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonPropertyOrder({ "bookingId", "hotelName", "roomType", "checkIn", "checkOut", "totalPrice", "status" })
public class BookingResponseDTO {
    private Long id;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalPrice;
    private String status;

    private String username;
    private String roomType;
    private String hotelName;

}