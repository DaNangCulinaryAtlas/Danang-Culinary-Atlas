package com.atlasculinary.services.impl;

import com.atlasculinary.dtos.*;
import com.atlasculinary.entities.*;
import com.atlasculinary.enums.AccountStatus;
import com.atlasculinary.enums.RoleLevel;
import com.atlasculinary.exceptions.PasswordResetException;
import com.atlasculinary.repositories.*;
import com.atlasculinary.services.AuthService;
import com.atlasculinary.services.NotificationService;
import com.atlasculinary.utils.JwtUtil;
import com.atlasculinary.utils.NameUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthServiceImpl.class.getName());
  private final AccountRepository accountRepository;
  private final RoleRepository roleRepository;
  private final AccountRoleMapRepository accountRoleMapRepository;
  private final UserRepository userRepository;
  private final AdminRepository adminRepository;
  private final VendorRepository vendorRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final AuthenticationManager authenticationManager;
  private final NotificationService notificationService;
  
  @Value("${jwt.refresh.expiration}")
  private Long refreshTokenExpirationMs;

  @Override
  @Transactional
  public void signUp(SignUpRequest signUpRequest) {
    if (accountRepository.existsByEmail(signUpRequest.getEmail())) {
      throw new RuntimeException("Email đã tồn tại trong hệ thống");
    }

    Account account = new Account();
    account.setEmail(signUpRequest.getEmail());
//    System.out.println(passwordEncoder.encode(signUpRequest.getPassword()));
    account.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
    account.setStatus(AccountStatus.ACTIVE);

    List<Role> roleList = roleRepository.findAll();
    for (var role: roleList) {
        LOGGER.severe(role.getRoleName() + " " +  role.getDescription());
    }
    Role role = roleRepository.findByRoleName(signUpRequest.getRole().name())
            .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

    Account savedAccount = accountRepository.save(account);

    AccountRoleMap accountRoleMap = new AccountRoleMap();
    accountRoleMap.setAccount(savedAccount);
    accountRoleMap.setRole(role);
    accountRoleMap.setAccountId(savedAccount.getAccountId());
    accountRoleMap.setRoleId(role.getRoleId());

    accountRoleMapRepository.save(accountRoleMap);

    // Tạo  tương ứng với role
    createForAccount(savedAccount, signUpRequest.getRole());
  }

  private void createForAccount(Account account, com.atlasculinary.enums.AccountRole role) {
    switch (role) {
      case USER:
        UserProfile user = new UserProfile();
        user.setAccount(account);
        userRepository.save(user);
        break;

      case ADMIN:
        AdminProfile admin = new AdminProfile();
        admin.setAccount(account);
        admin.setRoleLevel(RoleLevel.MODERATOR); // Default role level
        adminRepository.save(admin);
        break;

      case VENDOR:
        VendorProfile vendor = new VendorProfile();
        vendor.setAccount(account);
        vendorRepository.save(vendor);
        break;

      default:
        throw new RuntimeException("Role không được hỗ trợ");
    }
  }

  @Override
  public LoginResponse login(LoginRequest loginRequest) {
    // Kiểm tra trạng thái tài khoản trước khi xác thực
    Account account = accountRepository.findByEmail(loginRequest.getEmail())
        .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không chính xác"));

    if (account.getStatus() == AccountStatus.BLOCKED) {
      throw new RuntimeException("Tài khoản của bạn đã bị khóa");
    }
    if (account.getStatus() == AccountStatus.DELETED) {
      throw new RuntimeException("Tài khoản của bạn đã bị xóa");
    }

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.getEmail(),
              loginRequest.getPassword()
          )
      );

      var roleMapList = accountRoleMapRepository.findByAccountIdWithRole(account.getAccountId());

      var roles = roleMapList.stream()
        .map(roleMap -> {
          return roleMap.getRole().getRoleName();
        })
        .collect(Collectors.toList());
      LOGGER.severe("Roles" + roles);
      String token = jwtUtil.generateToken(account.getEmail(), roles);
      
      // Generate refresh token
      String refreshTokenStr = jwtUtil.generateRefreshToken(account.getEmail());
      saveRefreshToken(account, refreshTokenStr);
      
      String fullName = account.getFullName();
      String email = account.getEmail();
      UUID accountId = account.getAccountId();
      fullName = NameUtil.resolveName(fullName, email);
      return LoginResponse.builder()
          .token(token)
          .refreshToken(refreshTokenStr)
          .accountId(accountId)
          .email(email)
          .fullName(fullName)
          .avatarUrl(account.getAvatarUrl())
          .roles(roles)
          .build();

    } catch (AuthenticationException e) {
      throw new RuntimeException("Email hoặc mật khẩu không chính xác");
    }
  }

  @Override
  @Transactional
  public void changePassword(String email, ChangePasswordRequest changePasswordRequest) {
    if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
      throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không khớp");
    }

    Account account = accountRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

    if (account.getStatus() == AccountStatus.BLOCKED) {
      throw new RuntimeException("Tài khoản đã bị khóa");
    }
    if (account.getStatus() == AccountStatus.DELETED) {
      throw new RuntimeException("Tài khoản đã bị xóa");
    }

    if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), account.getPassword())) {
      throw new RuntimeException("Mật khẩu hiện tại không đúng");
    }

    if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), account.getPassword())) {
      throw new RuntimeException("Mật khẩu mới phải khác mật khẩu hiện tại");
    }

    account.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
    accountRepository.save(account);

    LOGGER.info("Đổi mật khẩu thành công cho tài khoản: " + email);
  }

  @Override
  @Transactional
  public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
    Account account = accountRepository.findByEmail(forgotPasswordRequest.getEmail())
        .orElseThrow(() -> new PasswordResetException("Không tìm thấy tài khoản với email: " + forgotPasswordRequest.getEmail()));

    if (account.getStatus() == AccountStatus.BLOCKED) {
      throw new PasswordResetException("Tài khoản đã bị khóa");
    }
    if (account.getStatus() == AccountStatus.DELETED) {
      throw new PasswordResetException("Tài khoản đã bị xóa");
    }

    // Kiểm tra xem đã có token chưa sử dụng không
    if (passwordResetTokenRepository.existsByAccountAndUsedFalse(account)) {
      throw new PasswordResetException("Yêu cầu đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra email của bạn.");
    }

    // Tạo token mới
    String token = UUID.randomUUID().toString();
    PasswordResetToken resetToken = new PasswordResetToken();
    resetToken.setToken(token);
    resetToken.setAccount(account);
    resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
    resetToken.setUsed(false);

    passwordResetTokenRepository.save(resetToken);

    // Gửi email
    PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
    passwordResetRequest.setAccountId(account.getAccountId());
    passwordResetRequest.setResetToken(token);
    passwordResetRequest.setPlatform(forgotPasswordRequest.getPlatform());
    
    notificationService.sendPasswordResetRequest(passwordResetRequest);

    LOGGER.info("Đã tạo token reset password cho tài khoản: " + account.getEmail() + " (platform: " + forgotPasswordRequest.getPlatform() + ")");
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
    if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())) {
      throw new PasswordResetException("Mật khẩu mới và xác nhận mật khẩu không khớp");
    }

    PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(resetPasswordRequest.getToken())
        .orElseThrow(() -> new PasswordResetException("Token không hợp lệ hoặc đã được sử dụng"));

    if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new PasswordResetException("Token đã hết hạn");
    }

    Account account = resetToken.getAccount();
    
    if (account.getStatus() == AccountStatus.BLOCKED) {
      throw new PasswordResetException("Tài khoản đã bị khóa");
    }
    if (account.getStatus() == AccountStatus.DELETED) {
      throw new PasswordResetException("Tài khoản đã bị xóa");
    }

    // Cập nhật mật khẩu
    account.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
    accountRepository.save(account);

    // Đánh dấu token đã sử dụng
    passwordResetTokenRepository.markTokenAsUsed(resetPasswordRequest.getToken(), LocalDateTime.now());

    LOGGER.info("Đặt lại mật khẩu thành công cho tài khoản: " + account.getEmail());
  }

  @Override
  public boolean validateResetToken(String token) {
    PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(token)
        .orElse(null);
    
    if (resetToken == null) {
      return false;
    }
    
    return resetToken.getExpiresAt().isAfter(LocalDateTime.now());
  }
  
  @Override
  @Transactional
  public RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
    String requestRefreshToken = refreshTokenRequest.getRefreshToken();
    
    RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
        .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));
    
    if (refreshToken.getRevoked()) {
      throw new RuntimeException("Refresh token đã bị thu hồi");
    }
    
    if (refreshToken.isExpired()) {
      refreshTokenRepository.delete(refreshToken);
      throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại");
    }
    
    Account account = refreshToken.getAccount();
    
    if (account.getStatus() == AccountStatus.BLOCKED) {
      throw new RuntimeException("Tài khoản đã bị khóa");
    }
    if (account.getStatus() == AccountStatus.DELETED) {
      throw new RuntimeException("Tài khoản đã bị xóa");
    }
    
    // Get user roles
    var roleMapList = accountRoleMapRepository.findByAccountIdWithRole(account.getAccountId());
    var roles = roleMapList.stream()
        .map(roleMap -> roleMap.getRole().getRoleName())
        .collect(Collectors.toList());
    
    // Generate new access token
    String newAccessToken = jwtUtil.generateToken(account.getEmail(), roles);
    
    // Generate new refresh token and revoke old one
    String newRefreshToken = jwtUtil.generateRefreshToken(account.getEmail());
    refreshToken.setRevoked(true);
    refreshTokenRepository.save(refreshToken);
    saveRefreshToken(account, newRefreshToken);
    
    return RefreshTokenResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();
  }
  
  @Override
  @Transactional
  public void logout(String refreshToken) {
    RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
        .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));
    
    if (token.getRevoked()) {
      throw new RuntimeException("Refresh token đã được thu hồi trước đó");
    }
    
    token.setRevoked(true);
    refreshTokenRepository.save(token);
    LOGGER.info("Đã thu hồi refresh token cho account: " + token.getAccount().getEmail());
  }
  
  private void saveRefreshToken(Account account, String token) {
    RefreshToken refreshToken = RefreshToken.builder()
        .token(token)
        .account(account)
        .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
        .revoked(false)
        .build();
    refreshTokenRepository.save(refreshToken);
  }
}
