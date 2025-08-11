package com.blockcloud.controller;

import com.blockcloud.dto.RequestDto.BlockSaveRequestDto;
import com.blockcloud.dto.ResponseDto.BlockGetResponseDto;
import com.blockcloud.dto.ResponseDto.BlockSaveResponseDto;
import com.blockcloud.dto.common.ResponseDto;
import com.blockcloud.dto.oauth.CustomUserDetails;
import com.blockcloud.service.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Block API", description = "블록 아키텍처 저장 및 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/block")
public class BlockController {

	private final BlockService blockService;

	@Operation(
		summary = "블록 아키텍처 저장",
		description = "특정 프로젝트(`projectId`)에 대한 블록 인프라 데이터를 저장합니다. "
	)
	@PostMapping("/{projectId}")
	public ResponseDto<BlockSaveResponseDto> saveBlocks(
		@Parameter(description = "블록을 저장할 프로젝트 ID", required = true)
		@PathVariable Long projectId,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "저장할 블록 아키텍처 데이터",
			required = true
		)
		@Valid @RequestBody BlockSaveRequestDto requestDto,
		Authentication authentication) {

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		return ResponseDto.ok(
			blockService.saveBlocks(projectId, requestDto, userDetails.getUsername()));
	}

	@Operation(
		summary = "블록 아키텍처 불러오기",
		description = "특정 프로젝트(`projectId`)의 블록 및 연결 정보를 불러옵니다. "
	)
	@GetMapping("/{projectId}")
	public ResponseDto<BlockGetResponseDto> getBlocks(
		@Parameter(description = "블록을 조회할 프로젝트 ID", required = true) @PathVariable Long projectId,
		Authentication authentication) {

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		return ResponseDto.ok(blockService.getBlocks(projectId, userDetails.getUsername()));
	}
}