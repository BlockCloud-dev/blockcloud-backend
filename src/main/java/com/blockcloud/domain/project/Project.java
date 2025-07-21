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

	@Column(name = "create_at", updatable = false)
	private LocalDateTime createAt;

	@Column(name = "update_at")
	private LocalDateTime updateAt;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<ProjectUser> members = new ArrayList<>();


	@PrePersist
	public void onCreate() {
		this.createAt = LocalDateTime.now();
		this.updateAt = LocalDateTime.now();
	}

	@PreUpdate
	public void onUpdate() {
		this.updateAt = LocalDateTime.now();
	}

	public void updateInfo(String name, String description) {
		this.name = name;
		this.description = description;
		this.updateAt = LocalDateTime.now();
	}
}