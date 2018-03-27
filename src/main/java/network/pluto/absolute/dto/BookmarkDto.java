package network.pluto.absolute.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BookmarkDto {

    @JsonProperty("paper_id")
    public long paperId;
    public boolean bookmarked = false;

    public BookmarkDto(long paperId) {
        this.paperId = paperId;
    }

    public static BookmarkDto bookmarked(long paperId) {
        BookmarkDto dto = new BookmarkDto(paperId);
        dto.bookmarked = true;
        return dto;
    }

    public static BookmarkDto available(long paperId) {
        return new BookmarkDto(paperId);
    }

}
