package com.notfound.timecampusserver.service;

public interface CaptchaVerificationService {

    void verifyLoginToken(String capToken);

    default void verifySeedreamGenerationToken(String capToken) {
        verifyLoginToken(capToken);
    }
}
