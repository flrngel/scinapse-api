package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import network.pluto.absolute.model.Bookmark;

import java.time.LocalDateTime;

public class BookmarkDto {

    @JsonProperty("paper_id")
    public long paperId;

    public boolean bookmarked = false;

    @JsonProperty("created_at")
    public LocalDateTime createdAt;

    public PaperDto paper;

    public BookmarkDto(long paperId) {
        this.paperId = paperId;
    }

    public static BookmarkDto bookmarked(Bookmark bookmark) {
        BookmarkDto dto = new BookmarkDto(bookmark.getPaperId());
        dto.bookmarked = true;
        dto.createdAt = bookmark.getCreatedAt();
        return dto;
    }

    public static BookmarkDto available(long paperId) {
        return new BookmarkDto(paperId);
    }

}
