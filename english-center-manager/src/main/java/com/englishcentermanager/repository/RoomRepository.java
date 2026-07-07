package com.englishcentermanager.repository;

import com.englishcentermanager.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomCode(String roomCode);

    boolean existsByRoomCode(String roomCode);

    List<Room> findByActiveTrue();

    List<Room> findByRoomNameContainingIgnoreCase(String keyword);
}
