package com.notfound.timetrackserver.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String saveUgcImage(Long userId, MultipartFile file);

}
