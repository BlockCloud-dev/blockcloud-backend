package com.blockcloud.service;

import com.blockcloud.domain.project.Project;
import com.blockcloud.domain.project.ProjectRepository;
import com.blockcloud.domain.project.ProjectUser;
import com.blockcloud.domain.project.ProjectUserRepository;
import com.blockcloud.dto.RequestDto.ProjectRequestDto;
import com.blockcloud.dto.ResponseDto.ProjectResponseDto;
import com.blockcloud.domain.user.User;
import com.blockcloud.domain.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 프로젝트 관련 비즈니스 로직을 처리하는 서비스 클래스 프로젝트 생성, 조회, 수정, 삭제 기능을 제공
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final ProjectUserRepository projectUserRepository;
	private final UserRepository userRepository;

	/**
	 * 새로운 프로젝트를 생성하고 생성자와의 매핑을 저장
	 *
	 * @param dto   프로젝트 생성에 필요한 정보 (이름, 설명 등)
	 * @param email 프로젝트 생성자의 이메일
	 * @return 생성된 프로젝트 정보가 담긴 응답 DTO
	 * @throws IllegalArgumentException 해당 이메일의 사용자를 찾을 수 없는 경우 발생
	 */
	@Transactional
	public ProjectResponseDto create(ProjectRequestDto dto, String email) {
		// 이메일로 사용자 조회
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		// 프로젝트 엔티티 생성 및 저장
		Project project = Project.builder()
			.name(dto.getName())
			.description(dto.getDescription())
			.build();
		projectRepository.save(project);

		// 프로젝트와 사용자 매핑 정보 저장
		ProjectUser link = ProjectUser.builder()
			.user(user)
			.project(project)
			.build();
		projectUserRepository.save(link);

		// 응답 DTO 생성 및 반환
		return ProjectResponseDto.builder()
			.success(true)
			.project(ProjectResponseDto.ProjectInfo.builder()
				.id(project.getId())
				.name(project.getName())
				.description(project.getDescription())
				.createdAt(project.getCreatedAt())
				.updatedAt(project.getUpdatedAt())
				.build())
			.build();
	}

	/**
	 * 페이징 처리된 프로젝트 목록을 조회
	 *
	 * @param lastId 마지막으로 조회된 프로젝트 ID (첫 조회 시 null)
	 * @param size   한 번에 조회할 프로젝트 수
	 * @return 프로젝트 목록과 페이징 정보
	 */
	@Transactional(readOnly = true)
	public Map<String, Object> findNext(Long lastId, int size) {
		// 페이징 정보 생성 (0 페이지, size 개수)
		Pageable pageable = PageRequest.of(0, size);
		// 프로젝트 목록 조회
		List<Project> projects = projectRepository.findNextProjects(lastId, pageable);

		// 다음 페이지 존재 여부 확인
		boolean hasNext = projects.size() == size;

		// 결과 맵 생성 및 반환
		Map<String, Object> result = new HashMap<>();
		result.put("success", true);
		result.put("projects", projects);
		result.put("hasNext", hasNext);
		return result;
	}


	/**
	 * 기존 프로젝트의 정보를 수정
	 *
	 * @param projectId 수정할 프로젝트 ID
	 * @param dto       수정할 프로젝트 정보 (이름, 설명 등)
	 * @return 수정된 프로젝트 정보가 담긴 응답 DTO
	 * @throws IllegalArgumentException 해당 ID의 프로젝트를 찾을 수 없는 경우 발생
	 */
	@Transactional
	public ProjectResponseDto update(Long projectId, ProjectRequestDto dto) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

		project.updateInfo(dto.getName(), dto.getDescription());

		return ProjectResponseDto.builder()
			.success(true)
			.project(ProjectResponseDto.ProjectInfo.builder()
				.id(project.getId())
				.name(project.getName())
				.description(project.getDescription())
				.createdAt(project.getCreatedAt())
				.updatedAt(project.getUpdatedAt())
				.build())
			.build();
	}

	/**
	 * 프로젝트를 삭제
	 *
	 * @param projectId 삭제할 프로젝트 ID
	 * @return 삭제 성공 여부 (true: 성공, false: 해당 ID의 프로젝트가 없음)
	 */
	@Transactional
	public boolean delete(Long projectId) {
		// 프로젝트 존재 여부 확인
		if (!projectRepository.existsById(projectId)) {
			return false;
		}
		// 프로젝트 삭제
		projectRepository.deleteById(projectId);
		return true;
	}
}