package com.blockcloud.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CommonResponse<T> {
	private boolean success;
	private T data;

	public static <T> CommonResponse<T> ok(T data) {
		return CommonResponse.<T>builder()
			.success(true)
			.data(data)
			.build();
	}
}
