package io.scinapse.api.data.scinapse.repository;

import io.scinapse.api.dto.CommentWrapper;

import java.util.List;
import java.util.Map;

public interface CommentRepositoryCustom {
    Map<Long, CommentWrapper> getDefaultComments(List<Long> paperIds);
}
