package com.blockcloud.domain.project;

import com.blockcloud.domain.global.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import com.blockcloud.domain.deployment.Deployment;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "block_info", columnDefinition = "LONGTEXT")
	private String blockInfo;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	@Builder.Default
	private List<ProjectUser> members = new ArrayList<>();

	@OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private ProjectShareToken shareToken;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<Deployment> deployments = new ArrayList<>();
	
	public void updateInfo(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public void updateArchitecture(String blockInfo) {
		this.blockInfo = blockInfo;
	}
}
