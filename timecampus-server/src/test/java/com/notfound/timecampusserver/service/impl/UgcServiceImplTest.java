package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.entity.MediaEntity;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.mapper.PoiMapper;
import com.notfound.timecampusserver.service.LogService;
import com.notfound.timecampusserver.service.MediaFileService;
import com.notfound.timecampusserver.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UgcServiceImplTest {

    private final MediaMapper mediaMapper = mock(MediaMapper.class);
    private final PoiMapper poiMapper = mock(PoiMapper.class);
    private final StorageService storageService = mock(StorageService.class);
    private final LogService logService = mock(LogService.class);
    private final MediaFileService mediaFileService = mock(MediaFileService.class);
    private final UgcServiceImpl service = new UgcServiceImpl(
            mediaMapper,
            poiMapper,
            storageService,
            new MediaStructMapper(mediaFileService),
            logService
    );

    @Test
    void uploadCreatesPendingHiddenReviewCandidate() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "img".getBytes());
        when(poiMapper.existsById(7L)).thenReturn(true);
        when(storageService.store(file, 3L)).thenReturn("/uploads/photo.jpg");
        when(mediaFileService.previewUrl(null, "/uploads/photo.jpg")).thenReturn("/api/v1/media/20/file");

        var result = service.upload(file, 7L, 2000, "old gate", null, 3L);

        assertThat(result.getPoiId()).isEqualTo(7L);
        assertThat(result.getType()).isEqualTo("ugc");
        assertThat(result.getImagePath()).isEqualTo("/api/v1/media/20/file");
        assertThat(result.getReviewStatus()).isEqualTo("pending");
        verify(mediaMapper).insert(any(MediaEntity.class));
    }

    @Test
    void uploadRejectsOutOfRangeYear() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "img".getBytes());
        when(poiMapper.existsById(7L)).thenReturn(true);

        assertThatThrownBy(() -> service.upload(file, 7L, 1952, "old gate", null, 3L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("year must be between");
    }

    @Test
    void approveMarksUgcApproved() {
        MediaEntity entity = ugc(9L);
        when(mediaMapper.findById(9L)).thenReturn(entity);

        service.approve(9L, 1L);

        verify(mediaMapper).updateReview(9L, "approved", null, 1L);
    }

    @Test
    void rejectRequiresReason() {
        assertThatThrownBy(() -> service.reject(9L, " ", 1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("reject reason is required");
    }

    private MediaEntity ugc(Long id) {
        MediaEntity entity = new MediaEntity();
        entity.setId(id);
        entity.setPoiId(7L);
        entity.setType("ugc");
        entity.setImagePath("/uploads/photo.jpg");
        entity.setYear(2000);
        entity.setReviewStatus("pending");
        return entity;
    }
}
