package com.blockcloud.domain.project;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * 페이징 처리를 위한 프로젝트 목록을 조회
     * 주어진 lastId보다 작은 ID를 가진 프로젝트들을 내림차순으로 조회
     *
     * @param lastId 마지막으로 조회된 프로젝트 ID (첫 조회 시 null)
     * @param pageable 페이징 정보 (페이지 크기, 정렬 등)
     * @return 조회된 프로젝트 목록
     */
    @Query("SELECT p FROM Project p WHERE (:lastId IS NULL OR p.id < :lastId) ORDER BY p.id DESC")
    List<Project> findNextProjects(@Param("lastId") Long lastId, Pageable pageable);
}