package com.blockcloud.domain.deployment;

import com.blockcloud.domain.global.BaseTimeEntity;
import com.blockcloud.domain.project.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deployment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private DeploymentStatus status;

	@Column(columnDefinition = "TEXT")
	private String message;

	@Column(columnDefinition = "LONGTEXT")
	private String terraformCode;

	@Column(columnDefinition = "LONGTEXT")
	private String output;

	@Column(name = "started_at")
	private LocalDateTime startedAt;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	public void updateStatus(DeploymentStatus status, String message) {
		this.status = status;
		this.message = message;
		if (status == DeploymentStatus.SUCCESS || status == DeploymentStatus.FAILED) {
			this.completedAt = LocalDateTime.now();
		}
	}

	public void setOutput(String output) {
		this.output = output;
	}
}
