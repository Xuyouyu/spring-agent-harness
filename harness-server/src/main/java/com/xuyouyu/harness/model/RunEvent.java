package com.xuyouyu.harness.model;

import java.time.Instant;

public record RunEvent(Instant timestamp, String type, String detail) {
}
