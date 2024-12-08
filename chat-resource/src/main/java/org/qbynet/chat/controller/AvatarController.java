package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.qbynet.chat.entity.Avatar;
import org.qbynet.chat.entity.Media;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.AddAvatarDTO;
import org.qbynet.chat.entity.dto.RemoveAvatarDTO;
import org.qbynet.chat.entity.vo.AvatarVO;
import org.qbynet.chat.service.ConversationService;
import org.qbynet.chat.service.MediaService;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("list")
    public ResponseEntity<RestBean<List<AvatarVO>>> list(@RequestParam(name = "user") String userId, @RequestParam(name = "conversation") String conversationId) {
        List<Avatar> avatars;
        if (userId != null) {
            avatars = userService.findAllAvatars(userService.findById(userId));
        } else if (conversationId != null) {
            avatars = conversationService.findAllAvatars(conversationService.findConversationById(conversationId));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Bad request"));
        }
        return ResponseEntity.ok(RestBean.success(avatars.stream().map(AvatarVO::from).toList()));
    }

    @GetMapping("image")
    public ResponseEntity<RestBean<?>> latestImage(@NotNull HttpServletResponse response, @RequestParam(required = false) String user, @RequestParam(required = false) String conversation, @RequestParam(name = "uncompressed", required = false, defaultValue = "false") boolean uncompressed) throws Exception {
        Avatar avatar;
        if (user != null) {
            avatar = userService.findLatestAvatar(userService.findById(user));
        } else if (conversation != null) {
            avatar = conversationService.findLatestAvatar(conversationService.findConversationById(conversation));
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

    @PostMapping("user/add")
    public ResponseEntity<RestBean<AvatarVO>> addAvatar(@RequestBody AddAvatarDTO dto, @RequestAttribute("user") User user) {
        try {
            Avatar avatar = userService.addAvatar(mediaService.findById(dto.getMedia()), user);
            return ResponseEntity.ok(RestBean.success(AvatarVO.from(avatar)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, e.getMessage()));
        }
    }

    @DeleteMapping("user/remove")
    public ResponseEntity<RestBean<?>> removeAvatar(@RequestBody RemoveAvatarDTO dto, @RequestAttribute("user") User user) {
        if (userService.isAvatarBelongsTo(dto.getAvatar(), user)) {
            userService.removeAvatar(dto.getAvatar());
            return ResponseEntity.ok(RestBean.success("Ok"));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.forbidden("Forbidden"));
    }
}
