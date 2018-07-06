package io.scinapse.api.dto;

import io.scinapse.api.model.Comment;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CommentWrapper {
    private long paperId;
    private long totalCount;
    private List<Comment> comments = new ArrayList<>();
}
