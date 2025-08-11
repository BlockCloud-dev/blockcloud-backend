package com.blockcloud.service;

import com.blockcloud.domain.project.Project;
import com.blockcloud.domain.project.ProjectRepository;
import com.blockcloud.domain.project.ProjectUser;
import com.blockcloud.domain.project.ProjectUserRepository;
import com.blockcloud.domain.user.User;
import com.blockcloud.domain.user.UserRepository;
import com.blockcloud.dto.RequestDto.ProjectRequestDto;
import com.blockcloud.dto.ResponseDto.ProjectListResponseDto;
import com.blockcloud.dto.ResponseDto.ProjectResponseDto;
import com.blockcloud.exception.CommonException;
import com.blockcloud.exception.error.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_ACCOUNT));

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

		return toProjectResponseDto(project);
	}

	/**
	 * 페이징 처리된 프로젝트 목록을 조회
	 *
	 * @param lastId 마지막으로 조회된 프로젝트 ID (첫 조회 시 null)
	 * @param size   한 번에 조회할 프로젝트 수
	 * @return 프로젝트 목록과 페이징 정보
	 */
	@Transactional(readOnly = true)
	public ProjectListResponseDto findNext(Long lastId, int size) {
		// 페이징 정보 생성 (0 페이지, size 개수)
		Pageable pageable = PageRequest.of(0, size);
		// 프로젝트 목록 조회
		List<Project> projects = projectRepository.findNextProjects(lastId, pageable);

		List<ProjectResponseDto> projectDtos = projects.stream()
			.map(this::toProjectResponseDto)
			.collect(Collectors.toList());

		// 응답 DTO 생성 및 반환
		return ProjectListResponseDto.builder()
			.projects(projectDtos)
			.hasNext(projects.size() == size)
			.build();
	}


	/**
	 * 기존 프로젝트의 정보를 수정
	 *
	 * @param projectId 수정할 프로젝트 ID
	 * @param dto       수정할 프로젝트 정보 (이름, 설명 등)
	 * @param email     요청한 사용자의 이메일
	 * @return 수정된 프로젝트 정보가 담긴 응답 DTO
	 * @throws CommonException 해당 프로젝트를 찾을 수 없는 경우 또는 접근 권한이 없는 경우 발생
	 */
	@Transactional
	public ProjectResponseDto update(Long projectId, ProjectRequestDto dto, String email) {
		Project project = findProjectById(projectId);
		validateProjectMember(project, email);

		project.updateInfo(dto.getName(), dto.getDescription());
		return toProjectResponseDto(project);
	}

	/**
	 * 프로젝트를 삭제
	 *
	 * @param projectId 삭제할 프로젝트 ID
	 * @param email     요청한 사용자의 이메일
	 * @throws CommonException 해당 프로젝트를 찾을 수 없는 경우 또는 접근 권한이 없는 경우 발생
	 */
	@Transactional
	public void delete(Long projectId, String email) {
		Project project = findProjectById(projectId);
		validateProjectMember(project, email);
		projectRepository.delete(project);
	}

	/**
	 * 프로젝트 ID로 프로젝트를 조회하고 존재하지 않으면 예외 발생
	 *
	 * @param projectId 조회할 프로젝트 ID
	 * @return 해당 프로젝트 엔티티
	 * @throws CommonException 해당 프로젝트를 찾을 수 없는 경우 발생
	 */
	private Project findProjectById(Long projectId) {
		return projectRepository.findById(projectId)
			.orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_PROJECT));
	}

	/**
	 * 프로젝트의 멤버인지 검증
	 *
	 * @param project 조회할 프로젝트
	 * @param email   요청한 사용자의 이메일
	 * @throws CommonException 접근 권한이 없는 경우 발생
	 */
	private void validateProjectMember(Project project, String email) {
		boolean isMember = project.getMembers().stream()
			.anyMatch(member -> member.getUser().getEmail().equals(email));
		if (!isMember) {
			throw new CommonException(ErrorCode.ACCESS_DENIED);
		}
	}

	/**
	 * Project 엔티티를 ProjectResponseDto로 변환
	 *
	 * @param project 변환할 프로젝트 엔티티
	 * @return 변환된 응답 DTO
	 */
	private ProjectResponseDto toProjectResponseDto(Project project) {
		return ProjectResponseDto.builder()
			.id(project.getId())
			.name(project.getName())
			.description(project.getDescription())
			.createdAt(project.getCreatedAt())
			.updatedAt(project.getUpdatedAt())
			.build();
	}
}