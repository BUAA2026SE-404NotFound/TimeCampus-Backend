package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampusserver.config.CapProperties;
import com.notfound.timecampusserver.service.CaptchaVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class CaptchaVerificationServiceImplTest {

    @Test
    void springContextUsesProductionConstructor() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(CapProperties.class, () -> new CapProperties(false, "", "", null, null));
            context.register(CaptchaVerificationServiceImpl.class);

            context.refresh();

            assertThat(context.getBean(CaptchaVerificationService.class))
                    .isInstanceOf(CaptchaVerificationServiceImpl.class);
        }
    }
}
