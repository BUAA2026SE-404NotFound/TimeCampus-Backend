package com.notfound.timecampusserver.service.impl;

import com.notfound.timecampuscommon.api.ResultCode;
import com.notfound.timecampuscommon.exception.BizException;
import com.notfound.timecampuspojo.dto.OfficialMediaImportRequest;
import com.notfound.timecampuspojo.vo.ImportResultVO;
import com.notfound.timecampusserver.mapper.MediaMapper;
import com.notfound.timecampusserver.mapper.PoiMapper;
import com.notfound.timecampusserver.service.LogService;
import com.notfound.timecampusserver.service.MediaFileService;
import com.notfound.timecampusserver.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AdminMediaServiceImplTest {

    private final MediaMapper mediaMapper = mock(MediaMapper.class);
    private final PoiMapper poiMapper = mock(PoiMapper.class);
    private final LogService logService = mock(LogService.class);
    private final StorageService storageService = mock(StorageService.class);
    private final AdminMediaServiceImpl service = new AdminMediaServiceImpl(
            mediaMapper,
            poiMapper,
            new MediaStructMapper(mock(MediaFileService.class)),
            logService,
            storageService
    );

    @Test
    void uploadOfficialRejectsNullReviewerId() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "img".getBytes());

        assertThatThrownBy(() -> service.uploadOfficial(file, 1L, 2000, "old gate", null))
                .isInstanceOf(BizException.class)
                .satisfies(ex -> assertThat(((BizException) ex).getResultCode()).isEqualTo(ResultCode.UNAUTHORIZED));

        verifyNoInteractions(storageService);
        verify(mediaMapper, never()).insert(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void importOfficialRejectsOutOfRangeYearPerItem() {
        OfficialMediaImportRequest.OfficialMediaItem item = new OfficialMediaImportRequest.OfficialMediaItem();
        item.setPoiId(1L);
        item.setImagePath("/uploads/a.jpg");
        item.setYear(1952);
        OfficialMediaImportRequest request = new OfficialMediaImportRequest();
        request.setItems(List.of(item));

        ImportResultVO result = service.importOfficial(request);

        assertThat(result.getSuccessCount()).isEqualTo(0);
        assertThat(result.getFailCount()).isEqualTo(1);
        assertThat(result.getFailures()).hasSize(1);
        assertThat(result.getFailures().get(0).getReason()).contains("year must be between");
        verify(mediaMapper, never()).insertBatch(org.mockito.ArgumentMatchers.anyList());
        verifyNoInteractions(logService);
    }
}
