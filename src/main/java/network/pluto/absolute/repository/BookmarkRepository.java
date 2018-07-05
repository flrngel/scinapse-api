package network.pluto.absolute.repository;

import network.pluto.absolute.model.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Bookmark.BookmarkId> {
    Page<Bookmark> findByMemberIdOrderByCreatedAtDesc(long memberId, Pageable pageable);
    List<Bookmark> findByMemberIdAndPaperIdIn(long memberId, List<Long> paperIds);
}
