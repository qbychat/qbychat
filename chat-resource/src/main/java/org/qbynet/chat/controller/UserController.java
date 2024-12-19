package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.Status;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.entity.dto.EditProfileDTO;
import org.qbynet.chat.entity.dto.EditStatusDTO;
import org.qbynet.chat.entity.dto.StatusDTO;
import org.qbynet.chat.entity.vo.StatusVO;
import org.qbynet.chat.entity.vo.UserVO;
import org.qbynet.chat.service.UserService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

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
    public ResponseEntity<RestBean<UserVO>> editProfile(@RequestBody EditProfileDTO dto, @RequestAttribute("user") User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestBean.failure(401, "Profile not found, please register it first."));
        }
        user.setNickname(dto.getNickname());
        user.setUsername(dto.getUsername());
        user.setBio(dto.getBio());
        return ResponseEntity.ok(RestBean.success(UserVO.from(userService.update(user))));
    }

    @PostMapping("status")
    public ResponseEntity<RestBean<StatusVO>> status(@RequestBody StatusDTO dto, @RequestAttribute("user") User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestBean.failure(401, "Unauthorized, please register it first."));
        }
        if (Objects.equals(dto.getUsername(), "")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Username is required."));
        }
        User user1 = userService.findByUsername(dto.getUsername());
        return ResponseEntity.ok(RestBean.success(StatusVO.from(userService.getUserStatus(user1))));
    }

    @PostMapping("editStatus")
    public ResponseEntity<RestBean<StatusVO>> editStatus(@RequestBody EditStatusDTO dto, @RequestAttribute("user") User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestBean.failure(401, "Unauthorized, please register it first."));
        }
        if (Objects.equals(dto.getStatus(), null)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Status is required."));
        }
        Status status;
        switch (dto.getStatus()) {
            case 1 -> {
                if (dto.getText() == null)
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Text is required."));
                status = Status.builder().status(1).text(dto.getText()).build();
            }
            default -> {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Status is wrong."));
            }
        }
        userService.setUserStatus(status);
        return ResponseEntity.ok(RestBean.success(StatusVO.from(status)));
    }
}
