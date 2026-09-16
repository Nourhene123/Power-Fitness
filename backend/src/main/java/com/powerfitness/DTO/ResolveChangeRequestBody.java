package com.powerfitness.DTO;

import java.util.Map;

public record ResolveChangeRequestBody(Map<String, String> answers, String message) {}
