package io.scinapse.domain.data.scinapse.repository;

import io.scinapse.domain.dto.CommentWrapper;

import java.util.List;
import java.util.Map;

public interface CommentRepositoryCustom {
    Map<Long, CommentWrapper> getDefaultComments(List<Long> paperIds);
}
