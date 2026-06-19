package KTB4_gourmet_Week6.Assignment.controller;

import KTB4_gourmet_Week6.Assignment.dto.LoginRequestDto;
import KTB4_gourmet_Week6.Assignment.dto.UserResponseDto;
import KTB4_gourmet_Week6.Assignment.dto.UserSignupRequestDto;
import KTB4_gourmet_Week6.Assignment.dto.UserUpdateRequestDto;
import KTB4_gourmet_Week6.Assignment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto signup(@Valid @RequestBody UserSignupRequestDto request) {
        return userService.signup(request);
    }

    @PostMapping("/login")
    public UserResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return userService.login(request);
    }

    @GetMapping
    public List<UserResponseDto> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public UserResponseDto getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @PatchMapping("/{userId}")
    public UserResponseDto updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequestDto request
    ) {
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}