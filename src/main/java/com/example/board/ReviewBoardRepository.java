package com.example.board;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewBoardRepository extends JpaRepository<ReviewBoard, Long> {
    List<ReviewBoard> findByRegion(String region);
    Optional<ReviewBoard> findByRegionAndTitle(String region, String title);
}
