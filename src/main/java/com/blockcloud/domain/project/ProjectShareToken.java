package com.blockcloud.domain.project;

import com.blockcloud.domain.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 프로젝트 공유 토큰 엔티티 프로젝트를 외부에 공유할 때 사용하는 토큰을 관리합니다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "project_share_tokens")
public class ProjectShareToken extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 255)
	private String token;

	@Column(name = "expires_at")
	private LocalDateTime expiresAt;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	/**
	 * 프로젝트에 대한 공유 토큰을 생성합니다.
	 *
	 * @param project 공유할 프로젝트
	 * @return 생성된 공유 토큰
	 */
	public static ProjectShareToken createForProject(Project project) {
		String token = generateSecureToken();
		LocalDateTime expiresAt = LocalDateTime.now().plusDays(30); // 30일 후 만료

		return ProjectShareToken.builder()
			.token(token)
			.expiresAt(expiresAt)
			.isActive(true)
			.project(project)
			.build();
	}


	/**
	 * 토큰이 유효한지 확인합니다.
	 *
	 * @return 토큰이 활성화되어 있고 만료되지 않았으면 true
	 */
	public boolean isValid() {
		return this.isActive &&
			this.expiresAt != null &&
			this.expiresAt.isAfter(LocalDateTime.now());
	}

	/**
	 * 안전한 토큰을 생성합니다.
	 *
	 * @return 생성된 토큰 문자열
	 */
	private static String generateSecureToken() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
