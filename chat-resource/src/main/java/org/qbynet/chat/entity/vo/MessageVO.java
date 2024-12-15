package org.qbynet.chat.entity.vo;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Message;

import java.util.List;

@Data
public class MessageVO {
    private String id;
    private SenderVO sender;
    private String content;

    private String reply;
    private String redirect;
    private LinkPreviewVO linkPreview;
    private String language;

    private List<MediaVO> medias = List.of();

    private long sentAt;
    private long editAt = -1L;

    public static @NotNull MessageVO from(@NotNull Message source) {
        MessageVO vo = new MessageVO();
        vo.setId(source.getId());
        vo.setContent(source.getContent());
        vo.setLanguage(source.getLanguage());
        vo.setSentAt(source.getSentAt().getEpochSecond());
        if (source.getEditAt() != null) {
            vo.setEditAt(source.getEditAt().getEpochSecond());
        }
        if (source.getLinkPreview() != null) {
            vo.setLinkPreview(LinkPreviewVO.from(source.getLinkPreview()));
        }
        if (source.isAnonymous()) {
            vo.setSender(SenderVO.from(source.getSender()));
        }
        if (source.getReply() != null) {
            vo.setReply(source.getReply().getId());
        }
        if (source.getRedirect() != null) {
            vo.setRedirect(source.getRedirect().getId());
        }
        vo.setMedias(source.getMedias().stream().map(MediaVO::from).toList());
        return vo;
    }
}
