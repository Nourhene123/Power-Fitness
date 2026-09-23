package com.powerfitness.dto;

import java.util.Map;

public record ResolveChangeRequestBody(Map<String, String> answers, String message) {}
