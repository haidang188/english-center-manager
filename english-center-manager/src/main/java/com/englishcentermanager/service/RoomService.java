package com.englishcentermanager.service;

import com.englishcentermanager.entity.Room;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    List<Room> findAll();

    List<Room> findAllActive();

    Optional<Room> findById(Long id);

    Optional<Room> findByRoomCode(String roomCode);

    Room save(Room room);

    Room update(Long id, Room room);

    void activate(Long id);

    void deactivate(Long id);

    List<Room> searchByKeyword(String keyword);

    boolean existsByRoomCode(String roomCode);
}
