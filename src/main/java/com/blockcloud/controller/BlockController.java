package com.blockcloud.controller;

import com.blockcloud.dto.RequestDto.BlockSaveRequestDto;
import com.blockcloud.dto.ResponseDto.BlockGetResponseDto;
import com.blockcloud.dto.ResponseDto.BlockSaveResponseDto;
import com.blockcloud.dto.common.ResponseDto;
import com.blockcloud.dto.oauth.CustomUserDetails;
import com.blockcloud.service.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

	/**
	 * 블록 아키텍처 저장 특정 프로젝트(projectId)에 대한 블록 인프라 데이터를 저장합니다.
	 */
	@Operation(
		summary = "블록 아키텍처 저장",
		description = "특정 프로젝트(`projectId`)에 대한 블록 인프라 데이터를 저장합니다. "
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "저장 성공",
			content = @Content(schema = @Schema(implementation = ResponseDto.class),
				examples = @ExampleObject(value = """
					{
					  "success": true,
					  "message": "아키텍처가 성공적으로 저장되었습니다.",
					  "data": {
					    "projectId": 1,
					    "architectureName": "aws-architecture-2025-07-22",
					    "updatedAt": "2025-07-22T14:30:00"
					  }
					}
					"""))),
		@ApiResponse(responseCode = "400", description = "INVALID_ARGUMENT (요청 데이터 유효성 검증 실패)"),
		@ApiResponse(responseCode = "401", description = "UNAUTHORIZED (인증 실패)"),
		@ApiResponse(responseCode = "403", description = "ACCESS_DENIED (접근 권한 없음)"),
		@ApiResponse(responseCode = "404", description = "NOT_FOUND_PROJECT (프로젝트를 찾을 수 없음)")
	})
	@PostMapping("/{projectId}")
	public ResponseDto<BlockSaveResponseDto> saveBlocks(
		@Parameter(description = "블록을 저장할 프로젝트 ID", required = true)
		@PathVariable Long projectId,
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "저장할 블록 아키텍처 데이터",
			required = true,
			content = @Content(examples = @ExampleObject(value = """
				{
				   "createdAt": "2025-07-22T14:20:00",
				   "updatedAt": "2025-07-22T14:25:00",
				   "blocks": [
				     {
				       "id": "web-server",
				       "type": "aws_instance",
				       "position": {
				         "x": 100,
				         "y": 200
				       },
				       "properties": {
				         "ami": "ami-12345678",
				         "instance_type": "t2.micro",
				         "tags": {
				           "Name": "MyServer"
				         }
				       },
				       "connections": [
				         "subnet-1"
				       ]
				     }
				   ]
				 }
				"""))
		)
		@Valid @RequestBody BlockSaveRequestDto requestDto,
		Authentication authentication) {

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		return ResponseDto.ok(
			blockService.saveBlocks(projectId, requestDto, userDetails.getUsername()));
	}

	/**
	 * 블록 아키텍처 불러오기 특정 프로젝트(projectId)의 블록 및 연결 정보를 불러옵니다.
	 */
	@Operation(
		summary = "블록 아키텍처 불러오기",
		description = "특정 프로젝트(`projectId`)의 블록 및 연결 정보를 불러옵니다. "
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "조회 성공",
			content = @Content(examples = @ExampleObject(value = """
				{
				  "createdAt": "2025-07-22T14:20:00",
				  "updatedAt": "2025-07-22T14:25:00",
				  "blocks": [
				    {
				      "id": "web-server",
				      "type": "aws_instance",
				      "position": { "x": 100, "y": 200 },
				      "properties": { ... },
				      "connections": ["subnet-1"]
				    },
				    {
				      "id": "subnet-1",
				      "type": "aws_subnet",
				      "position": { "x": 200, "y": 300 },
				      "properties": { ... },
				      "connections": []
				    }
				  ]
				}
				"""))),
		@ApiResponse(responseCode = "401", description = "UNAUTHORIZED (인증 실패)"),
		@ApiResponse(responseCode = "403", description = "ACCESS_DENIED (접근 권한 없음)"),
		@ApiResponse(responseCode = "404", description = "NOT_FOUND_PROJECT (프로젝트를 찾을 수 없음)")
	})
	@GetMapping("/{projectId}")
	public ResponseDto<BlockGetResponseDto> getBlocks(
		@Parameter(description = "블록을 조회할 프로젝트 ID", required = true) @PathVariable Long projectId,
		Authentication authentication) {

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		return ResponseDto.ok(blockService.getBlocks(projectId, userDetails.getUsername()));
	}
}