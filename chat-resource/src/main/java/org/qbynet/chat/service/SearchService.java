package org.qbynet.chat.service;

import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.SearchResult;
import org.qbynet.chat.entity.User;

import java.util.List;

public interface SearchService {
    List<SearchResult> mixed(String content, User user, int page);

    List<SearchResult> media(@NotNull String content, @Nullable String contentType, int page);
}
