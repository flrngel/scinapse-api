package network.pluto.absolute.repository;

import network.pluto.absolute.dto.CommentWrapper;

import java.util.List;
import java.util.Map;

public interface CommentRepositoryCustom {
    Map<Long, CommentWrapper> getDefaultComments(List<Long> paperIds);
}
