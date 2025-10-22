package com.blockcloud.service;

import com.blockcloud.domain.project.Project;
import com.blockcloud.domain.project.ProjectRepository;
import com.blockcloud.domain.project.ProjectShareToken;
import com.blockcloud.domain.project.ProjectShareTokenRepository;
import com.blockcloud.dto.ResponseDto.ProjectShareTokenResponseDto;
import com.blockcloud.exception.CommonException;
import com.blockcloud.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 프로젝트 공유 토큰 관리 서비스
 */
@Service
@RequiredArgsConstructor
public class ProjectShareTokenService {

	private final ProjectRepository projectRepository;
	private final ProjectShareTokenRepository projectShareTokenRepository;

	@Value("${app.base-url}")
	private String baseUrl;

	@Value("${app.share-url-path}")
	private String shareUrlPath;

	/**
	 * 프로젝트의 공유 토큰을 생성하거나 조회합니다.
	 *
	 * @param projectId 프로젝트 ID
	 * @param email     요청한 사용자의 이메일
	 * @return 공유 토큰 정보
	 */
	@Transactional
	public ProjectShareTokenResponseDto getOrCreateShareToken(Long projectId, String email) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_PROJECT));

		validateProjectMember(project, email);

		ProjectShareToken shareToken = projectShareTokenRepository.findByProjectId(projectId)
			.orElseGet(() -> {
				ProjectShareToken newToken = ProjectShareToken.createForProject(project);
				return projectShareTokenRepository.save(newToken);
			});

		return toResponseDto(shareToken);
	}


	/**
	 * 사용자가 해당 프로젝트의 멤버인지 검증하는 메서드
	 */
	private void validateProjectMember(Project project, String email) {
		boolean isMember = project.getMembers().stream()
			.anyMatch(member -> member.getUser().getEmail().equals(email));
		if (!isMember) {
			throw new CommonException(ErrorCode.ACCESS_DENIED);
		}
	}

	/**
	 * ProjectShareToken 엔티티를 ResponseDto로 변환
	 */
	private ProjectShareTokenResponseDto toResponseDto(ProjectShareToken shareToken) {
		String shareUrl = buildShareUrl(shareToken.getProject().getId(), shareToken.getToken());

		return ProjectShareTokenResponseDto.builder()
			.projectId(shareToken.getProject().getId())
			.token(shareToken.getToken())
			.shareUrl(shareUrl)
			.createdAt(shareToken.getCreatedAt())
			.expiresAt(shareToken.getExpiresAt())
			.build();
	}

	/**
	 * 공유 URL을 생성합니다.
	 *
	 * @param projectId 프로젝트 ID
	 * @param token     공유 토큰
	 * @return 생성된 공유 URL
	 */
	private String buildShareUrl(Long projectId, String token) {
		// baseUrl 끝에 슬래시가 있으면 제거
		String cleanBaseUrl =
			baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		// shareUrlPath 시작에 슬래시가 없으면 추가
		String cleanSharePath = shareUrlPath.startsWith("/") ? shareUrlPath : "/" + shareUrlPath;

		return String.format("%s%s/%d?token=%s", cleanBaseUrl, cleanSharePath, projectId, token);
	}
}