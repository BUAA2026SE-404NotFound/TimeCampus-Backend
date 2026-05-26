package com.notfound.timecampusserver.service;

public interface CaptchaVerificationService {

    void verifyLoginToken(String capToken);
}
