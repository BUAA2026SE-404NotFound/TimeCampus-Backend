package com.notfound.timecampusserver.service;

public interface SeedreamGenerationGuardService {

    void verifyBeforeGenerate(String capToken, String clientIp);
}
