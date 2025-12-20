package com.atlasculinary.securities;

import com.atlasculinary.entities.Account;
import com.atlasculinary.entities.AccountRoleMap;
import com.atlasculinary.entities.Action; // Import Action
import com.atlasculinary.repositories.AccountRepository;
import com.atlasculinary.repositories.RoleActionMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomAccountDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final RoleActionMapRepository roleActionMapRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + email));

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Duyệt qua từng Role của User
        for (AccountRoleMap map : account.getAccountRoleMapSet()) {
            String roleName = map.getRole().getRoleName();

            // 1. Kiểm tra user có được cấp phép (Active) ở Role này không?
            boolean isUserLicensed = Boolean.TRUE.equals(map.getLicensed());

            // Add Role gốc (VD: ROLE_VENDOR)
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));

            // 2. Dùng hàm MỚI VIẾT bên Repository để lấy List<Action>
            List<Action> actions = roleActionMapRepository.findActionsByRoleId(map.getRoleId());

            // 3. Logic lọc quyền
            for (Action action : actions) {
                // Check xem Action này có yêu cầu bằng lái không
                boolean requiresLicense = Boolean.TRUE.equals(action.getRequiresLicense());

                // Cấp quyền nếu: User đã Active HOẶC Action này không yêu cầu Active
                if (isUserLicensed || !requiresLicense) {
                    authorities.add(new SimpleGrantedAuthority(action.getActionCode()));
                }
            }
        }

        // Truyền list authorities ĐÃ LỌC vào UserDetails
        return new CustomAccountDetails(account, authorities);
    }
}