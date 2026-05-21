package com.notfound.timecampusserver.service;

import com.notfound.timecampuspojo.dto.CommentCreateRequest;
import com.notfound.timecampuspojo.vo.CommentVO;
import com.notfound.timecampuspojo.vo.NoteVO;

import java.util.List;

public interface CommentService {

    CommentVO create(CommentCreateRequest request, Long userId);

    List<CommentVO> listByTarget(String targetType, Long targetId);

    List<CommentVO> listMine(Long userId, String reviewStatus);

    List<CommentVO> listForAdmin(String targetType, Long targetId, String reviewStatus);

    CommentVO approve(Long id, Long reviewerId);

    CommentVO reject(Long id, String reason, Long reviewerId);

    NoteVO createNote(Long userId, Long poiId, String content);

    List<NoteVO> listMyNotes(Long userId, Long poiId, String reviewStatus);

    void deleteNote(Long id, Long userId);
}
