package com.blockcloud.service;

import com.blockcloud.domain.project.Project;
import com.blockcloud.domain.project.ProjectRepository;
import com.blockcloud.dto.RequestDto.BlockSaveRequestDto;
import com.blockcloud.dto.ResponseDto.BlockGetResponseDto;
import com.blockcloud.dto.ResponseDto.BlockSaveResponseDto;
import com.blockcloud.exception.CommonException;
import com.blockcloud.exception.error.ErrorCode;
import com.nimbusds.jose.shaded.gson.Gson;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 블록 아키텍처 관리를 위한 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class BlockService {

	private final ProjectRepository projectRepository;

	/**
	 * 프로젝트의 블록 아키텍처 정보를 저장합니다.
	 *
	 * @param projectId 저장할 프로젝트의 ID
	 * @param dto       블록 정보가 담긴 요청 DTO
	 * @return 저장된 블록 정보를 담은 DTO (데이터 부분)
	 * @throws CommonException 해당 프로젝트를 찾을 수 없는 경우
	 */
	@Transactional
	public BlockSaveResponseDto saveBlocks(Long projectId, BlockSaveRequestDto dto, String email) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_PROJECT));

		validateProjectMember(project, email);

		String blockInfoJson = new Gson().toJson(dto.getBlocks());
		project.updateArchitecture(blockInfoJson);
		projectRepository.save(project);

		return BlockSaveResponseDto.builder()
			.projectId(project.getId())
			.architectureName(project.getName() + "-" + LocalDateTime.now())
			.updatedAt(project.getUpdatedAt())
			.build();
	}

	/**
	 * 프로젝트의 블록 아키텍처 정보를 조회합니다.
	 *
	 * @param projectId 조회할 프로젝트의 ID
	 * @param email     요청한 사용자의 이메일
	 * @return 블록 아키텍처 정보가 담긴 응답 DTO (데이터 부분)
	 * @throws CommonException 해당 프로젝트를 찾을 수 없는 경우 또는 접근 권한이 없는 경우
	 */
	@Transactional(readOnly = true)
	public BlockGetResponseDto getBlocks(Long projectId, String email) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_PROJECT));

		validateProjectMember(project, email);

		return BlockGetResponseDto.builder()
			.createdAt(project.getCreatedAt())
			.updatedAt(project.getUpdatedAt())
			.blocks(new Gson().fromJson(project.getBlockInfo(), Object.class))
			.build();
	}

	/**
	 * 사용자가 해당 프로젝트의 멤버인지 검증하는 메서드
	 *
	 * @param project 검증할 프로젝트
	 * @param email   요청한 사용자의 이메일
	 * @throws CommonException 접근 권한이 없는 경우
	 */
	private void validateProjectMember(Project project, String email) {
		boolean isMember = project.getMembers().stream()
			.anyMatch(member -> member.getUser().getEmail().equals(email));
		if (!isMember) {
			throw new CommonException(ErrorCode.ACCESS_DENIED);
		}
	}
}