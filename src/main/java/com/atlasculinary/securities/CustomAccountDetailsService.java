package com.atlasculinary.securities;

import com.atlasculinary.entities.Account;
import com.atlasculinary.entities.AccountRoleMap;
import com.atlasculinary.entities.Action;
import com.atlasculinary.entities.RoleActionMap; // Import entity bảng trung gian
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

        // Duyệt qua từng Role mà User sở hữu (Ví dụ: VENDOR, USER)
        for (AccountRoleMap accountRoleMap : account.getAccountRoleMapSet()) {
            String roleName = accountRoleMap.getRole().getRoleName();

            // 1. Kiểm tra trạng thái cấp phép (Licensed) của User đối với Role này

            boolean isUserLicensed = Boolean.TRUE.equals(accountRoleMap.getLicensed());

            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));

            // 2. Lấy danh sách Quyền từ bảng trung gian (RoleActionMap)

            List<RoleActionMap> rolePermissions = roleActionMapRepository.findByRoleIdWithAction(accountRoleMap.getRoleId());

            // 3. Logic lọc quyền (Filter Logic)
            for (RoleActionMap ram : rolePermissions) {
                Action action = ram.getAction();

                boolean requiresLicense = Boolean.TRUE.equals(ram.getRequiresLicense());


                if (isUserLicensed || !requiresLicense) {
                    authorities.add(new SimpleGrantedAuthority(action.getActionCode()));
                }
            }
        }

        // Truyền list authorities ĐÃ ĐƯỢC LỌC vào UserDetails
        return new CustomAccountDetails(account, authorities);
    }
}