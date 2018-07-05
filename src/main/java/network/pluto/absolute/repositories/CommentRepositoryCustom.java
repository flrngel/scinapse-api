package network.pluto.absolute.repositories;

import network.pluto.absolute.dto.CommentWrapper;

import java.util.List;
import java.util.Map;

public interface CommentRepositoryCustom {
    Map<Long, CommentWrapper> getDefaultComments(List<Long> paperIds);
}
