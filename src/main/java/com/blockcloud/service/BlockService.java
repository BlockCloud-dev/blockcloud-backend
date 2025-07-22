package com.blockcloud.service;

import com.blockcloud.domain.project.Project;
import com.blockcloud.domain.project.ProjectRepository;
import com.blockcloud.dto.RequestDto.BlockSaveRequestDto;
import com.blockcloud.dto.ResponseDto.BlockSaveResponseDto;
import com.nimbusds.jose.shaded.gson.Gson;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockService {

	private final ProjectRepository projectRepository;

	@Transactional
	public BlockSaveResponseDto saveBlocks(Long projectId, BlockSaveRequestDto dto) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new IllegalArgumentException("해당 프로젝트를 찾을 수 없습니다."));

		String blockInfoJson = new Gson().toJson(dto.getBlocks());
		project.updateArchitecture(blockInfoJson);

		projectRepository.save(project);

		return BlockSaveResponseDto.builder()
			.success(true)
			.message("아키텍처가 성공적으로 저장되었습니다.")
			.data(BlockSaveResponseDto.BlockInfo.builder()
				.projectId(project.getId())
				.architectureName(project.getName() + "-" + LocalDateTime.now())
				.updatedAt(project.getUpdateAt())
				.build())
			.build();
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getBlocks(Long projectId) {
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new IllegalArgumentException("해당 프로젝트를 찾을 수 없습니다."));

		Map<String, Object> response = new HashMap<>();
		response.put("createdAt", project.getCreateAt());
		response.put("updatedAt", project.getUpdateAt());
		response.put("blocks",
			new Gson().fromJson(project.getBlockInfo(), Object.class));
		return response;
	}
}