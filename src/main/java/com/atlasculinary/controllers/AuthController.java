package com.atlasculinary.controllers;

import com.atlasculinary.dtos.*;
import com.atlasculinary.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for authentication and password management")
public class AuthController {

  private final AuthService authService;
  
  @Value("${app.deeplink.scheme}")
  private String deeplinkScheme;

  @Operation(summary = "Đăng ký tài khoản mới")
  @PostMapping("/signup")
  public ResponseEntity<ApiResponse> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
    authService.signUp(signUpRequest);
    ApiResponse response = ApiResponse.success("Đăng ký tài khoản thành công");
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Đăng nhập")
  @PostMapping("/login")
  public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    LoginResponse loginResponse = authService.login(loginRequest);
    ApiResponse response = ApiResponse.success("Đăng nhập thành công", loginResponse);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Làm mới access token bằng refresh token")
  @PostMapping("/refresh-token")
  public ResponseEntity<ApiResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
    RefreshTokenResponse refreshTokenResponse = authService.refreshToken(refreshTokenRequest);
    ApiResponse response = ApiResponse.success("Làm mới token thành công", refreshTokenResponse);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Đăng xuất - Thu hồi refresh token")
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
    authService.logout(refreshTokenRequest.getRefreshToken());
    ApiResponse response = ApiResponse.success("Đăng xuất thành công");
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Thay đổi mật khẩu (yêu cầu đăng nhập)")
  @PatchMapping("/change-password")
  public ResponseEntity<ApiResponse> changePassword(
      @Valid @RequestBody ChangePasswordRequest changePasswordRequest,
      Authentication authentication) {
    String email = authentication.getName();
    authService.changePassword(email, changePasswordRequest);
    ApiResponse response = ApiResponse.success("Thay đổi mật khẩu thành công");
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Quên mật khẩu - Gửi email reset")
  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
    authService.forgotPassword(forgotPasswordRequest);
    ApiResponse response = ApiResponse.success("Đã gửi email hướng dẫn đặt lại mật khẩu. Vui lòng kiểm tra hộp thư của bạn.");
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Đặt lại mật khẩu với token")
  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
    authService.resetPassword(resetPasswordRequest);
    ApiResponse response = ApiResponse.success("Đặt lại mật khẩu thành công");
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Kiểm tra tính hợp lệ của token reset password")
  @GetMapping("/validate-reset-token")
  public ResponseEntity<ApiResponse> validateResetToken(@RequestParam String token) {
    boolean isValid = authService.validateResetToken(token);
    if (isValid) {
      ApiResponse response = ApiResponse.success("Token hợp lệ");
      return ResponseEntity.ok(response);
    } else {
      ApiResponse response = ApiResponse.error("Token không hợp lệ hoặc đã hết hạn");
      return ResponseEntity.badRequest().body(response);
    }
  }
  
  @Operation(summary = "Deep link redirect cho mobile app - Reset password")
  @GetMapping("/deeplink/reset-password")
  public RedirectView deeplinkResetPassword(@RequestParam String token) {
    // Validate token trước khi redirect
    boolean isValid = authService.validateResetToken(token);
    
    if (!isValid) {
      // Nếu token không hợp lệ, redirect về error page hoặc deeplink với error
      RedirectView redirectView = new RedirectView();
      redirectView.setUrl(deeplinkScheme + "://reset-password?error=invalid_token");
      redirectView.setStatusCode(HttpStatus.FOUND);
      return redirectView;
    }
    
    // Redirect sang deeplink scheme của app
    RedirectView redirectView = new RedirectView();
    redirectView.setUrl(deeplinkScheme + "://reset-password?token=" + token);
    redirectView.setStatusCode(HttpStatus.FOUND);
    return redirectView;
  }
}
