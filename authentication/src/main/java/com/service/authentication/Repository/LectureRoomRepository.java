package com.service.authentication.Repository;


import com.service.authentication.LectureRooms.LectureRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectureRoomRepository extends JpaRepository<LectureRoom, String> {
    LectureRoom findByRoomName(String roomName);
}
