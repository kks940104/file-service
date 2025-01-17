package org.anonymous.global.configs;

import lombok.RequiredArgsConstructor;
import org.anonymous.member.MemberUtil;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;


/**
 * AuditorAware<String> 데이터가 있는 모든 프레임워크는 이게 있음
 */
@Lazy
@Component
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<String> {

    private final MemberUtil memberUtils;

    @Override
    public Optional getCurrentAuditor() {
        String email = null;
        if (memberUtils.isLogin()) {
            email = memberUtils.getMember().getEmail();
        }
        return Optional.ofNullable(email);
    }
}
