package network.pluto.absolute.service;

import lombok.RequiredArgsConstructor;
import network.pluto.absolute.model.Bookmark;
import network.pluto.absolute.repository.BookmarkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    public Page<Bookmark> getBookmarks(long memberId, Pageable pageable) {
        return bookmarkRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
    }

    public Bookmark find(long memberId, long paperId) {
        Bookmark.BookmarkId bookmarkId = Bookmark.BookmarkId.of(memberId, paperId);
        return bookmarkRepository.findOne(bookmarkId);
    }

    public List<Bookmark> findIn(long memberId, List<Long> paperIds) {
        return bookmarkRepository.findByMemberIdAndPaperIdIn(memberId, paperIds);
    }

    @Transactional
    public Bookmark save(long memberId, long paperId) {
        Bookmark bookmark = new Bookmark();
        bookmark.setMemberId(memberId);
        bookmark.setPaperId(paperId);
        return bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void delete(Bookmark bookmark) {
        bookmarkRepository.delete(bookmark);
    }

}
