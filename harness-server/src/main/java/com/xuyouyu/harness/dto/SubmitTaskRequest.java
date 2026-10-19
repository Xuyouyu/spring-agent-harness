package com.xuyouyu.harness.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitTaskRequest(@NotBlank String prompt) {

}