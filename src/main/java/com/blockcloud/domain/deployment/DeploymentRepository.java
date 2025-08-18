package com.blockcloud.domain.deployment;

import com.blockcloud.domain.project.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

	@Query("SELECT d FROM Deployment d WHERE d.project = :project ORDER BY d.createdAt DESC")
	List<Deployment> findByProjectOrderByCreatedAtDesc(@Param("project") Project project, Pageable pageable);

	@Query("SELECT COUNT(d) > 0 FROM Deployment d WHERE d.project = :project AND d.createdAt > :lastId")
	boolean existsByProjectAndCreatedAtAfter(@Param("project") Project project, @Param("lastId") Long lastId);
}
