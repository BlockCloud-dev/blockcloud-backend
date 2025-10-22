package com.blockcloud.domain.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 프로젝트 공유 토큰 리포지토리
 */
public interface ProjectShareTokenRepository extends JpaRepository<ProjectShareToken, Long> {

	/**
	 * 프로젝트 ID로 공유 토큰을 조회합니다.
	 */
	@Query("SELECT pst FROM ProjectShareToken pst WHERE pst.project.id = :projectId")
	Optional<ProjectShareToken> findByProjectId(@Param("projectId") Long projectId);

	/**
	 * 토큰 문자열로 공유 토큰을 조회합니다.
	 */
	Optional<ProjectShareToken> findByToken(String token);

	/**
	 * 프로젝트 ID와 토큰으로 유효한 공유 토큰을 조회합니다.
	 */
	@Query("SELECT pst FROM ProjectShareToken pst WHERE pst.project.id = :projectId AND pst.token = :token")
	Optional<ProjectShareToken> findValidTokenByProjectIdAndToken(@Param("projectId") Long projectId, @Param("token") String token);
}
