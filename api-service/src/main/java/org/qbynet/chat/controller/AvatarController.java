package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.*;
import org.qbynet.chat.entity.dto.AddAvatarDTO;
import org.qbynet.chat.entity.dto.RemoveAvatarDTO;
import org.qbynet.chat.entity.vo.AvatarVO;
import org.qbynet.chat.service.AvatarService;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.MediaService;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avatar")
public class AvatarController {
    @Resource
    UserService userService;

    @Resource
    ConversationService conversationService;

    @Resource
    MediaService mediaService;

    @Resource
    AvatarService avatarService;

    @GetMapping("list")
    public ResponseEntity<RestBean<List<AvatarVO>>> list(@RequestParam(name = "user", required = false) String userId, @RequestParam(name = "conversation", required = false) String conversationId) {
        List<Avatar> avatars;
        if (userId != null) {
            avatars = avatarService.getAllAvatars(userService.findById(userId));
        } else if (conversationId != null) {
            avatars = avatarService.getAllAvatars(conversationService.findConversationById(conversationId));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Bad request"));
        }
        return ResponseEntity.ok(RestBean.success(avatars.stream().map(AvatarVO::from).toList()));
    }

    @GetMapping("image")
    public ResponseEntity<RestBean<?>> latestImage(@NotNull HttpServletResponse response, @RequestParam(required = false) String user, @RequestParam(required = false) String conversation, @RequestParam(name = "uncompressed", required = false, defaultValue = "false") boolean uncompressed) throws Exception {
        Avatar avatar;
        if (user != null) {
            avatar = avatarService.getLatestAvatar(userService.findById(user));
        } else if (conversation != null) {
            avatar = avatarService.getLatestAvatar(conversationService.findConversationById(conversation));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Bad request"));
        }
        if (avatar == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "This user haven't uploaded an avatar."));
        }
        // get the file
        Media media;
        if (uncompressed) {
            media = avatar.getMedia();
        } else {
            media = avatar.getMedia().getCompressed();
        }
        // send redirect
        response.sendRedirect("/api/media/" + media.getId() + "/raw");
        return null;
    }

    @PostMapping("user")
    @Secured("SCOPE_avatar.manage")
    public ResponseEntity<RestBean<AvatarVO>> addUserAvatar(@RequestBody AddAvatarDTO dto, @RequestAttribute("user") User user) {
        try {
            Avatar avatar = avatarService.addAvatar(mediaService.findById(dto.getMedia()), user);
            return ResponseEntity.ok(RestBean.success(AvatarVO.from(avatar)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, e.getMessage()));
        }
    }

    @DeleteMapping("user")
    @Secured("SCOPE_avatar.manage")
    public ResponseEntity<RestBean<?>> removeUserAvatar(@RequestBody RemoveAvatarDTO dto, @RequestAttribute("user") User user) {
        Avatar avatar = avatarService.getAvatar(dto.getAvatar());
        if (avatarService.isAvatarBelongsTo(avatar, user)) {
            avatarService.removeAvatar(avatar);
            return ResponseEntity.ok(RestBean.success("Ok"));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.forbidden("Forbidden"));
    }

    @Secured("SCOPE_avatar.manage")
    @PostMapping("conversation")
    public ResponseEntity<RestBean<AvatarVO>> addConversationAvatar(@RequestBody AddAvatarDTO dto, @RequestAttribute("user") User user) {
        Conversation conversation = conversationService.findConversationById(dto.getConversation());
        Member member = conversationService.findMember(conversation, user);
        if (member == null || !member.hasPermissions(MemberPermission.MANAGE_AVATARS)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.failure(403, "Forbidden"));
        }
        try {
            Avatar avatar = avatarService.addAvatar(mediaService.findById(dto.getMedia()), conversation);
            return ResponseEntity.ok(RestBean.success(AvatarVO.from(avatar)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, e.getMessage()));
        }
    }

    @Secured("SCOPE_avatar.manage")
    @DeleteMapping("conversation")
    public ResponseEntity<RestBean<?>> removeConversationAvatar(@RequestBody RemoveAvatarDTO dto, @RequestAttribute("user") User user) {
        Conversation conversation = conversationService.findConversationById(dto.getConversation());
        Member member = conversationService.findMember(conversation, user);
        Avatar avatar = avatarService.getAvatar(dto.getAvatar());
        if (member == null || !member.hasPermissions(MemberPermission.MANAGE_AVATARS) || avatarService.isAvatarBelongsTo(avatar, conversation)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.failure(403, "Forbidden"));
        }
        try {
            avatarService.removeAvatar(avatar);
            return ResponseEntity.ok(RestBean.success("Ok"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, e.getMessage()));
        }
    }

    @PostMapping("bot")
    @Secured("SCOPE_avatar.manage")
    public ResponseEntity<RestBean<AvatarVO>> addBotAvatar(@RequestBody AddAvatarDTO dto, @RequestAttribute("user") User user) {
        Bot bot = userService.findBot(dto.getBot());
        if (!bot.isBelongTo(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.failure(403, "Forbidden"));
        }
        try {
            Avatar avatar = avatarService.addAvatar(mediaService.findById(dto.getMedia()), bot.getBot());
            return ResponseEntity.ok(RestBean.success(AvatarVO.from(avatar)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, e.getMessage()));
        }
    }

    @DeleteMapping("bot")
    @Secured("SCOPE_avatar.manage")
    public ResponseEntity<RestBean<?>> removeBotAvatar(@RequestBody RemoveAvatarDTO dto, @RequestAttribute("user") User user) {
        Bot bot = userService.findBot(dto.getBot());
        Avatar avatar = avatarService.getAvatar(dto.getAvatar());
        if (bot == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "Bot not found"));
        }
        if (!bot.isBelongTo(user) || !avatarService.isAvatarBelongsTo(avatar, bot.getBot())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.failure(403, "Forbidden"));
        }
        avatarService.removeAvatar(avatar);
        return ResponseEntity.ok(RestBean.success("Ok"));
    }
}
