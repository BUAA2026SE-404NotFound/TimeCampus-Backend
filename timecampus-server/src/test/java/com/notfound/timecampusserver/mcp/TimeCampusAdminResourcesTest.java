package com.notfound.timecampusserver.mcp;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpResource;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class TimeCampusAdminResourcesTest {

    private static final Pattern URI_VARIABLE = Pattern.compile("\\{([^}]+)}");

    @Test
    void resourceUriTemplateParametersUseStringForMcpScanner() {
        for (Method method : TimeCampusAdminResources.class.getDeclaredMethods()) {
            McpResource resource = method.getAnnotation(McpResource.class);
            if (resource == null) {
                continue;
            }
            Set<String> uriVariables = uriVariables(resource.uri());
            for (Parameter parameter : method.getParameters()) {
                McpArg arg = parameter.getAnnotation(McpArg.class);
                if (arg != null && uriVariables.contains(arg.name())) {
                    assertThat(parameter.getType())
                            .as(method.getName() + " URI variable " + arg.name())
                            .isEqualTo(String.class);
                }
            }
        }
    }

    private Set<String> uriVariables(String uri) {
        Matcher matcher = URI_VARIABLE.matcher(uri);
        return matcher.results()
                .map(result -> result.group(1))
                .collect(Collectors.toSet());
    }
}
