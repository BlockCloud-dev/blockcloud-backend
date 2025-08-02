package com.blockcloud.domain.project;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 프로젝트와 사용자 간의 매핑 정보를 관리하는 리포지토리 인터페이스
 */
public interface ProjectUserRepository extends JpaRepository<ProjectUser, Long> {

}