package network.pluto.absolute.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.dto.CommentWrapper;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.models.Comment;
import network.pluto.absolute.models.Member;
import network.pluto.absolute.repositories.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    @Transactional
    public Comment saveComment(long paperId, @Nonnull Comment comment) {
        comment.setPaperId(paperId);
        return commentRepository.save(comment);
    }

    public Comment find(long commentId) {
        return commentRepository.findOne(commentId);
    }

    public Page<Comment> findByPaperId(long paperId, Pageable pageable) {
        return commentRepository.findByPaperIdOrderByUpdatedAtDesc(paperId, pageable);
    }

    public void setDefaultComments(List<PaperDto> paperDtos) {
        Map<Long, CommentWrapper> commentWrapperMap = commentRepository.getDefaultComments(paperDtos.stream().map(PaperDto::getId).collect(Collectors.toList()));
        paperDtos.forEach(dto -> {
            CommentWrapper wrapper = commentWrapperMap.get(dto.getId());
            if (wrapper != null) {
                List<CommentDto> commentDtos = wrapper.getComments()
                        .stream()
                        .map(CommentDto::new)
                        .collect(Collectors.toList());

                dto.setCommentCount(wrapper.getTotalCount());
                dto.setComments(commentDtos);
            }
        });
    }

    public void setDefaultComments(PaperDto paperDto) {
        setDefaultComments(Collections.singletonList(paperDto));
    }

    @Transactional
    public Comment updateComment(Comment old, Comment update) {
        old.setComment(update.getComment());
        return old;
    }

    @Transactional
    public void deleteComment(Comment comment) {
        commentRepository.delete(comment);
    }

    public long getCount(Member createdBy) {
        return commentRepository.countByCreatedBy(createdBy);
    }

}
