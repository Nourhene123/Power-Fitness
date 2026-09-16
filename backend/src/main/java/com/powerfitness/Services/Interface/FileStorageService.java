package com.powerfitness.Services.Interface;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /** Validates, stores under {@code products/}, and returns the public {@code /uploads/...} URL. */
    String storeProductImage(MultipartFile file);
}
