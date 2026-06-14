package com.notfound.timecampuspojo.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntitySchemaContractTest {

    @Test
    void userEntityShouldMatchSchemaContract() throws NoSuchFieldException {
        assertFieldTypes(UserEntity.class, Map.of(
                "id", Long.class,
                "openId", String.class,
                "nickname", String.class,
                "avatarUrl", String.class,
                "identity", String.class,
                "enrollYear", Integer.class,
                "createTime", LocalDateTime.class,
                "updateTime", LocalDateTime.class
        ));
    }

    @Test
    void poiEntityShouldMatchSchemaContract() throws NoSuchFieldException {
        assertFieldTypes(PoiEntity.class, Map.of(
                "id", Long.class,
                "name", String.class,
                "latitude", BigDecimal.class,
                "longitude", BigDecimal.class,
                "description", String.class,
                "funFact", String.class,
                "status", Integer.class,
                "createTime", LocalDateTime.class,
                "updateTime", LocalDateTime.class
        ));
    }

    @Test
    void mediaEntityShouldMatchSchemaContract() throws NoSuchFieldException {
        assertFieldTypes(MediaEntity.class, Map.ofEntries(
                Map.entry("id", Long.class),
                Map.entry("poiId", Long.class),
                Map.entry("type", String.class),
                Map.entry("imagePath", String.class),
                Map.entry("year", Integer.class),
                Map.entry("description", String.class),
                Map.entry("uploadUserId", Long.class),
                Map.entry("reviewStatus", String.class),
                Map.entry("rejectReason", String.class),
                Map.entry("reviewTime", LocalDateTime.class),
                Map.entry("reviewerId", Long.class),
                Map.entry("createTime", LocalDateTime.class),
                Map.entry("updateTime", LocalDateTime.class)
        ));
    }

    @Test
    void favoriteEntityShouldMatchSchemaContract() throws NoSuchFieldException {
        assertFieldTypes(FavoriteEntity.class, Map.of(
                "id", Long.class,
                "userId", Long.class,
                "targetType", String.class,
                "targetId", Long.class,
                "createTime", LocalDateTime.class
        ));
    }

    @Test
    void commentEntityShouldMatchSchemaContract() throws NoSuchFieldException {
        assertFieldTypes(CommentEntity.class, Map.ofEntries(
                Map.entry("id", Long.class),
                Map.entry("userId", Long.class),
                Map.entry("targetType", String.class),
                Map.entry("targetId", Long.class),
                Map.entry("content", String.class),
                Map.entry("reviewStatus", String.class),
                Map.entry("rejectReason", String.class),
                Map.entry("reviewTime", LocalDateTime.class),
                Map.entry("reviewerId", Long.class),
                Map.entry("createTime", LocalDateTime.class),
                Map.entry("updateTime", LocalDateTime.class)
        ));
    }

    @Test
    void logEntityShouldMatchSchemaContract() throws NoSuchFieldException {
        assertFieldTypes(LogEntity.class, Map.of(
                "id", Long.class,
                "operatorType", String.class,
                "operatorId", Long.class,
                "type", String.class,
                "action", String.class,
                "targetType", String.class,
                "targetId", Long.class,
                "detail", String.class,
                "createTime", LocalDateTime.class
        ));
    }

    @Test
    void adminEntityShouldMatchSchemaContract() throws NoSuchFieldException {
        assertFieldTypes(AdminEntity.class, Map.of(
                "id", Long.class,
                "adminId", Long.class,
                "adminName", String.class,
                "password", String.class,
                "status", Integer.class,
                "lastLoginTime", LocalDateTime.class,
                "createTime", LocalDateTime.class,
                "updateTime", LocalDateTime.class
        ));
    }

    private void assertFieldTypes(Class<?> type, Map<String, Class<?>> expected) throws NoSuchFieldException {
        for (Map.Entry<String, Class<?>> entry : expected.entrySet()) {
            Field field = type.getDeclaredField(entry.getKey());
            assertEquals(entry.getValue(), field.getType(), "field type mismatch: " + type.getSimpleName() + "." + entry.getKey());
        }
    }
}



