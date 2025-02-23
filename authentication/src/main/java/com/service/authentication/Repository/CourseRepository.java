package com.service.authentication.Repository;

import com.service.authentication.Course.CourseModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CourseRepository extends JpaRepository<CourseModel, String> {
    Optional<CourseModel> findByCourseCode(String courseCode);

    @Query("SELECT COUNT(c) > 0 FROM CourseModel c JOIN c.lecturers l WHERE c.id = :courseId AND l.id = :lecturerId")
    boolean existsByLecturerIdAndCourseId(String lecturerId, String courseId);
}
