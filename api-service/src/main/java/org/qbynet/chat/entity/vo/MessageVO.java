package org.qbynet.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Message;
import org.qbynet.chat.entity.MessageType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {
    private String id;
    private SenderVO sender;
    private String conversation;

    private String content;
    private StickerVO sticker;

    private MessageVO reply;
    private MessageVO redirect;
    private LinkPreviewVO linkPreview;
    private String language;

    private MessageType type;

    private List<MediaVO> medias = List.of();

    private long sentAt;
    private long editAt = -1L;

    private boolean pinned;
    private boolean bot = false;
    private boolean myself = false;

    private int readCount = 0;


    public static @NotNull MessageVO from(@NotNull Message source) {
        MessageVO vo = new MessageVO();
        vo.setId(source.getId());
        vo.setSender(SenderVO.from(source.getSender()));
        vo.setConversation(source.getConversation().getId());
        vo.setContent(source.getContent());
        vo.setSticker(StickerVO.from(source.getSticker()));
        vo.setLanguage(source.getLanguage());
        vo.setSentAt(source.getSentAt().getEpochSecond());
        vo.setPinned(source.isPinned());
        vo.setType(source.getType());
        vo.setLinkPreview(LinkPreviewVO.from(source.getLinkPreview()));
        if (source.getEditAt() != null) {
            vo.setEditAt(source.getEditAt().getEpochSecond());
        }
        if (source.getReply() != null) {
            vo.setReply(MessageVO.from(source.getReply()));
        }
        if (source.getRedirect() != null) {
            vo.setRedirect(MessageVO.from(source.getRedirect()));
        }
        vo.setMedias(source.getMedias().stream().map(MediaVO::from).toList());
        return vo;
    }

    public static @NotNull MessageVOBuilder builder(@NotNull Message source) {
        MessageVOBuilder builder = new MessageVOBuilder()
            .id(source.getId())
            .conversation(source.getConversation().getId())
            .content(source.getContent())
            .sticker(StickerVO.from(source.getSticker()))
            .type(source.getType())
            .language(source.getLanguage())
            .sentAt(source.getSentAt().getEpochSecond())
            .pinned(source.isPinned())
            .medias(source.getMedias().stream().map(MediaVO::from).toList())
            .linkPreview(LinkPreviewVO.from(source.getLinkPreview()));
        builder.sender(SenderVO.from(source.getSender()));
        if (source.getEditAt() != null) {
            builder.editAt(source.getEditAt().getEpochSecond());
        }
        if (source.getReply() != null) {
            builder.reply(MessageVO.from(source.getReply()));
        }
        if (source.getRedirect() != null) {
            builder.redirect(MessageVO.from(source.getRedirect()));
        }
        return builder;
    }
}
