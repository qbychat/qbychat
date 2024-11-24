package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.EditProfileDTO;
import org.qbynet.chat.entity.vo.UserVO;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    UserService userService;

    @GetMapping("profile")
    public ResponseEntity<RestBean<UserVO>> profile(@RequestAttribute("user") User user) {
        return ResponseEntity.ok(RestBean.success(UserVO.fromUser(user)));
    }

    @PostMapping("profile")
    public ResponseEntity<RestBean<UserVO>> editProfile(@RequestBody EditProfileDTO dto, @RequestAttribute("user") User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestBean.failure(401, "Profile not found, please register it first."));
        }
        user.setNickname(dto.getNickname());
        user.setUsername(dto.getUsername());
        user.setBio(dto.getBio());
        return ResponseEntity.ok(RestBean.success(UserVO.fromUser(userService.update(user))));
    }
}
