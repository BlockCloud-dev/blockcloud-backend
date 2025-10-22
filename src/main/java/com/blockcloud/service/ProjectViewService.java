package com.blockcloud.service;

import com.blockcloud.domain.project.Project;
import com.blockcloud.domain.project.ProjectShareToken;
import com.blockcloud.domain.project.ProjectShareTokenRepository;
import com.blockcloud.dto.ResponseDto.ProjectViewResponseDto;
import com.blockcloud.exception.CommonException;
import com.blockcloud.exception.error.ErrorCode;
import com.nimbusds.jose.shaded.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 프로젝트 공개 조회를 위한 서비스 클래스
 * URL 공유를 위한 인증 없는 프로젝트 조회 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class ProjectViewService {

	private final ProjectShareTokenRepository projectShareTokenRepository;

	/**
	 * 프로젝트 ID와 공유 토큰으로 프로젝트 정보와 블록 아키텍처를 조회합니다.
	 *
	 * @param projectId  조회할 프로젝트 ID
	 * @param shareToken 공유 토큰
	 * @return 프로젝트 정보와 블록 아키텍처가 담긴 응답 DTO
	 * @throws CommonException 해당 프로젝트를 찾을 수 없는 경우 또는 유효하지 않은 토큰인 경우
	 */
	@Transactional(readOnly = true)
	public ProjectViewResponseDto getProjectView(Long projectId, String shareToken) {
		// 프로젝트와 토큰 유효성 검증
		ProjectShareToken token = projectShareTokenRepository
			.findValidTokenByProjectIdAndToken(projectId, shareToken)
			.orElseThrow(() -> new CommonException(ErrorCode.ACCESS_DENIED));

		// 토큰 만료 및 활성화 상태 검증
		if (!token.isValid()) {
			throw new CommonException(ErrorCode.ACCESS_DENIED);
		}

		Project project = token.getProject();

		// 블록 정보를 JSON에서 Object로 변환
		Object blocks = null;
		if (project.getBlockInfo() != null && !project.getBlockInfo().isEmpty()) {
			blocks = new Gson().fromJson(project.getBlockInfo(), Object.class);
		}

		return ProjectViewResponseDto.builder()
			.id(project.getId())
			.name(project.getName())
			.description(project.getDescription())
			.createdAt(project.getCreatedAt())
			.updatedAt(project.getUpdatedAt())
			.blocks(blocks)
			.build();
	}
}