package com.notfound.timecampusserver.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeepSeekChatPropertiesTest {

    @Test
    void defaultsUseStableDeepSeekChatModel() {
        DeepSeekChatProperties properties = new DeepSeekChatProperties();

        assertThat(properties.getModel()).isEqualTo("deepseek-chat");
        assertThat(properties.getEndpoint()).isEqualTo("https://api.deepseek.com/v1/chat/completions");
    }

    @Test
    void generationBoundsStayConservative() {
        DeepSeekChatProperties properties = new DeepSeekChatProperties();

        properties.setTemperature(3);
        properties.setMaxTokens(99);
        assertThat(properties.getTemperature()).isEqualTo(1.0);
        assertThat(properties.getMaxTokens()).isEqualTo(256);

        properties.setTemperature(-1);
        properties.setMaxTokens(9999);
        assertThat(properties.getTemperature()).isEqualTo(0.0);
        assertThat(properties.getMaxTokens()).isEqualTo(4096);
    }
}
