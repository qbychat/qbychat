package org.qbynet.chat.service.impl;

import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.vo.*;
import org.qbynet.chat.repository.*;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.SearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SearchServiceImpl implements SearchService {
    @Value("${qbychat.search.page.limit}")
    int searchPageLimit;

    @Value("${qbychat.search.media}")
    boolean canSearchMedia;

    @Resource
    ConversationRepository conversationRepository;

    @Resource
    UserRepository userRepository;

    @Resource
    InviteLinkRepository inviteLinkRepository;

    @Resource
    MessageRepository messageRepository;

    @Resource
    MediaRepository mediaRepository;

    @Resource
    ConversationService conversationService;

    @Override
    public List<SearchResult> mixed(@NotNull String content, User user, int page) {
        List<SearchResult> results = new ArrayList<>();
        if (content.startsWith("@")) {
            // search username & group link
            String cut = content.substring(1);
            List<User> users = userRepository.findAllByUsernameStartsWithIgnoreCase(cut, PageRequest.of(page, searchPageLimit)).toList();
            List<Conversation> conversations = conversationRepository.findAllByLinkStartsWithIgnoreCase(cut, PageRequest.of(page, searchPageLimit)).toList();

            List<SearchResult> userSearchResults = users.stream().map(it -> SearchResult.builder().user(UserVO.from(it)).type(SearchResultType.USER).build()).toList();
            results.addAll(userSearchResults);
            List<SearchResult> conversationSearchResults = conversations.stream().map(it -> SearchResult.builder().conversation(ConversationVO.from(it)).type(SearchResultType.CONVERSATION).build()).toList();
            results.addAll(conversationSearchResults);
        } else if (content.startsWith("+")) {
            // match invite links
            Optional<SearchResult> linkOptional = inviteLinkRepository.findByLinkAndExpireAtAfter(content, Instant.now()).map(it -> SearchResult.builder()
                    .inviteLink(InviteLinkVO.from(it))
                    .conversation(ConversationVO.from(it.getCreateBy().getConversation()))
                    .type(SearchResultType.INVITE_LINK).build()
            );
            linkOptional.ifPresent(results::add);
        }

        // search by nickname
        List<User> users = userRepository.findAllByNicknameContainingIgnoreCase(content, PageRequest.of(page, searchPageLimit)).toList();
        List<SearchResult> userSearchResults = users.stream().map(it -> SearchResult.builder().user(UserVO.from(it)).type(SearchResultType.USER).build()).toList();
        results.addAll(userSearchResults);

        // search by conversation name
        List<Conversation> conversations = conversationRepository.findAllByNameContainingIgnoreCase(content, PageRequest.of(page, searchPageLimit)).toList();
        List<SearchResult> conversationSearchResults = conversations.stream().map(it -> SearchResult.builder().conversation(ConversationVO.from(it)).type(SearchResultType.CONVERSATION).build()).toList();
        results.addAll(conversationSearchResults);

        // search for messages
        List<Conversation> joinedConversations = conversationService.list(user);
        joinedConversations.forEach(conversation -> {
            List<Message> messages = messageRepository.findAllByConversationAndContentContainingIgnoreCase(conversation, content, PageRequest.of(page, searchPageLimit)).toList();
            List<SearchResult> messageSearchResults = messages.stream().map(it -> SearchResult.builder().message(MessageVO.from(it)).type(SearchResultType.MESSAGE).build()).toList();
            results.addAll(messageSearchResults);
        });
        return results;
    }

    @Override
    public List<SearchResult> media(@NotNull String content, @Nullable String contentType, int page) {
        if (!canSearchMedia) {
            return List.of();
        }
        Page<Media> pageObj;
        if (contentType == null) {
            pageObj = mediaRepository.findAllByNameContainingIgnoreCase(content, PageRequest.of(page, searchPageLimit));
        } else {
            pageObj = mediaRepository.findAllByNameContainingIgnoreCaseAndContentType(content, contentType, PageRequest.of(page, searchPageLimit));
        }
        return new ArrayList<>(pageObj.stream().map(it -> SearchResult.builder()
                .media(MediaVO.from(it))
                .type(SearchResultType.MEDIA)
                .build()
        ).toList());
    }
}
