package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.EditProfileDTO;
import org.qbynet.chat.entity.dto.EditStatusDTO;
import org.qbynet.chat.entity.vo.StatusVO;
import org.qbynet.chat.entity.vo.UserVO;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    UserService userService;

    @GetMapping("profile")
    public ResponseEntity<RestBean<UserVO>> profile(@RequestAttribute("user") User user) {
        return ResponseEntity.ok(RestBean.success(UserVO.from(user)));
    }

    @PostMapping("profile")
    @Secured("SCOPE_profile.read")
    public ResponseEntity<RestBean<UserVO>> editProfile(@RequestBody EditProfileDTO dto, @RequestAttribute("user") User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestBean.failure(401, "Profile not found, please register it first."));
        }
        user.setNickname(dto.getNickname());
        user.setUsername(dto.getUsername());
        user.setBio(dto.getBio());
        return ResponseEntity.ok(RestBean.success(UserVO.from(userService.update(user))));
    }

    @GetMapping("status")
    public ResponseEntity<RestBean<StatusVO>> status(@RequestParam(required = false, name = "user") String targetUser, @RequestAttribute("user") User user) {
        if (targetUser == null) {
            return ResponseEntity.ok(RestBean.success(StatusVO.from(user)));
        }
        User user1 = userService.findById(targetUser);
        if (user1 != null && userService.canAccessStatus(user1, user)) {
            return ResponseEntity.ok(RestBean.success(StatusVO.from(user1)));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "User not found."));
    }

    @PostMapping("status")
    @Secured("SCOPE_status.edit")
    public ResponseEntity<RestBean<?>> editStatus(@RequestBody EditStatusDTO dto, @RequestAttribute("user") User user) {
        userService.setUserStatus(user, dto.getText());
        return ResponseEntity.ok(RestBean.success("Ok"));
    }
}
