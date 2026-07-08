package com.englishcentermanager.service;

import com.englishcentermanager.entity.Room;
import com.englishcentermanager.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Override
    public List<Room> findAllActive() {
        return roomRepository.findByActiveTrue();
    }

    @Override
    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    @Override
    public Optional<Room> findByRoomCode(String roomCode) {
        return roomRepository.findByRoomCode(roomCode);
    }

    @Override
    public Room save(Room room) {
        room.setActive(room.getActive() != null ? room.getActive() : true);
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        return roomRepository.save(room);
    }

    @Override
    public Room update(Long id, Room room) {
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng học"));

        existingRoom.setRoomCode(room.getRoomCode());
        existingRoom.setRoomName(room.getRoomName());
        existingRoom.setCapacity(room.getCapacity());
        existingRoom.setActive(room.getActive() != null ? room.getActive() : true);
        existingRoom.setUpdatedAt(LocalDateTime.now());

        return roomRepository.save(existingRoom);
    }

    @Override
    public void activate(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng học"));

        room.setActive(true);
        room.setUpdatedAt(LocalDateTime.now());
        roomRepository.save(room);
    }

    @Override
    public void deactivate(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng học"));

        room.setActive(false);
        room.setUpdatedAt(LocalDateTime.now());
        roomRepository.save(room);
    }

    @Override
    public List<Room> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return roomRepository.findAll();
        }

        String searchValue = keyword.trim();
        return roomRepository.findByRoomCodeContainingIgnoreCaseOrRoomNameContainingIgnoreCase(searchValue, searchValue);
    }

    @Override
    public boolean existsByRoomCode(String roomCode) {
        return roomRepository.existsByRoomCode(roomCode);
    }

    @Override
    public boolean existsByRoomCodeAndIdNot(String roomCode, Long id) {
        return roomRepository.existsByRoomCodeAndIdNot(roomCode, id);
    }
}
