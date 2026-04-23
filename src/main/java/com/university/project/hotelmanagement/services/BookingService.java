package com.university.project.hotelmanagement.services;

import com.university.project.hotelmanagement.dto.BookingRequestDTO;
import com.university.project.hotelmanagement.dto.BookingResponseDTO;
import com.university.project.hotelmanagement.dto.UserBookingDetailsDTO;
import com.university.project.hotelmanagement.entity.Booking;
import com.university.project.hotelmanagement.entity.Hotel;
import com.university.project.hotelmanagement.entity.Room;
import com.university.project.hotelmanagement.entity.UserEntity;
import com.university.project.hotelmanagement.exception.ResourceNotFoundException;
import com.university.project.hotelmanagement.repository.BookingRepository;
import com.university.project.hotelmanagement.repository.RoomRepository;
import com.university.project.hotelmanagement.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final EmailService mailService;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, UserRepository userRepository, EmailService mailService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found in database"));
    }

    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        UserEntity currentUser = getCurrentUser();

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found!"));
        long days = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (days <= 0) {
            throw new IllegalArgumentException("Check-out date must be after Check-in date");
        }
        boolean isBooked = bookingRepository.existsByRoomIdAndDateRange(
                request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate());

        if (isBooked) {
            throw new IllegalStateException("Room is already booked for these dates!");
        }
        Booking booking = new Booking();
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setRoom(room);
        booking.setUser(currentUser);
        booking.setHotel(room.getHotel());
        booking.setTotalGuests(request.getTotalGuests());
        booking.setTotalPrice(days * room.getPrice());
        booking.setStatus("CONFIRMED");

        Booking savedBooking = bookingRepository.save(booking);
        try {
            String displayName = (currentUser.getFirstName() != null) ?
                    currentUser.getFirstName() : currentUser.getEmail().split("@")[0];

            mailService.sendBookingConfirmation(
                    currentUser.getEmail(),
                    displayName,
                    room.getHotel().getName(),
                    room.getType(),
                    savedBooking.getTotalPrice()
            );
        } catch (Exception e) {
            System.err.println("Booking saved but Email failed: " + e.getMessage());
        }

        return mapToResponse(savedBooking);
    }

    public BookingResponseDTO getBookingByIdForAdmin(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
        return mapToResponse(booking);
    }

    public List<BookingResponseDTO> getBookingsByUser() {
        UserEntity currentUser = getCurrentUser();
        return bookingRepository.findByUser(currentUser)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }



    @Transactional
    public String cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        UserEntity currentUser = getCurrentUser();
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to cancel this booking!");
        }
        if ("CANCELLED".equals(booking.getStatus())) {
            return "Booking is already cancelled.";
        }
        if (booking.getCheckInDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot cancel a past or ongoing booking!");
        }
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        return "Booking cancelled successfully for " + booking.getRoom().getHotel().getName();
    }


    @Transactional(readOnly = true)
    public List<UserBookingDetailsDTO> getUserFullDetails() {
        UserEntity currentUser = getCurrentUser();

        List<Booking> bookings = bookingRepository.findAllBookingsByUserId(currentUser.getId());

        return bookings.stream().map(b -> {
            UserBookingDetailsDTO dto = new UserBookingDetailsDTO();
            dto.setBookingId(b.getId());
            dto.setUserName(currentUser.getFirstName()+" "+currentUser.getLastName());

            Hotel hotel = b.getRoom().getHotel();
            dto.setHotelName(hotel.getName());
            dto.setState(hotel.getState());
            dto.setDistrict(hotel.getDistrict());

            dto.setRoomType(b.getRoom().getType());
            dto.setCheckIn(b.getCheckInDate());
            dto.setCheckOut(b.getCheckOutDate());
            dto.setAmount(b.getTotalPrice());
            dto.setStatus(b.getStatus());

            return dto;
        }).collect(Collectors.toList());
    }

    private BookingResponseDTO mapToResponse(Booking b) {
        BookingResponseDTO response = new BookingResponseDTO();
        response.setId(b.getId());
        response.setHotelName(b.getRoom().getHotel().getName());
        response.setRoomType(b.getRoom().getType());
        response.setCheckInDate(b.getCheckInDate());
        response.setCheckOutDate(b.getCheckOutDate());
        response.setTotalPrice(b.getTotalPrice());
        response.setStatus(b.getStatus());
        response.setUsername(b.getUser().getFirstName()+" "+b.getUser().getLastName());
        return response;
    }

}