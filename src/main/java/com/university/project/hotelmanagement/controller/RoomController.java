package com.university.project.hotelmanagement.controller;

import com.university.project.hotelmanagement.dto.RoomResponseDTO;
import com.university.project.hotelmanagement.services.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/rooms")
public class RoomController {


    private  final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // View
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponseDTO> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    // Update
//    @PutMapping("/{id}")
//    public ResponseEntity<String> updateRoom(@PathVariable Long id,
//                                             @RequestBody RoomRequest roomDto,
//                                             @AuthenticationPrincipal UserDetails userDetails) {
//        roomService.updateRoom(id, roomDto, userDetails);
//        return ResponseEntity.ok("Room updated successfully!");
//    }

    // Delete
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deleteRoom(@PathVariable Long id,
//                                             @AuthenticationPrincipal UserDetails userDetails) {
//        roomService.deleteRoom(id, userDetails);
//        return ResponseEntity.ok("Room deleted successfully!");
//    }
}