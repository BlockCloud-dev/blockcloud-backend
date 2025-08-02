package com.blockcloud.domain.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<ProjectUser> members = new ArrayList<>();

	@Column(name = "block_info", columnDefinition = "LONGTEXT")
	private String blockInfo;  // JSON 형태로 블록 데이터 저장

	@Column(name = "connection_info", columnDefinition = "LONGTEXT")
	private String connectionInfo;  // JSON 형태로 연결 데이터 저장

	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 프로젝트 수정 시 업데이트 시간 갱신
	 */
	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 프로젝트 기본 정보(이름, 설명) 업데이트
	 */
	public void updateInfo(String name, String description) {
		this.name = name;
		this.description = description;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * 블록 및 연결 정보 업데이트
	 */
	public void updateArchitecture(String blockInfoJson) {
		this.blockInfo = blockInfoJson;
		this.connectionInfo = null;
		this.updateAt = LocalDateTime.now();
	}
}