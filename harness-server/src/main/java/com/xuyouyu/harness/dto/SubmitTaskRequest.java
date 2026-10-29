package com.xuyouyu.harness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitTaskRequest(@NotBlank @Size(max = 10000) String prompt) {

}
