package com.swadeshitech.prodhub.config;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import com.swadeshitech.prodhub.dto.UserResponse;
import com.swadeshitech.prodhub.services.UserService;
import com.swadeshitech.prodhub.utils.UserContextUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {

    @Autowired
    private UserService userService;

	@Override
    public Optional<String> getCurrentAuditor() {
        String uidx = UserContextUtil.getUserIdFromRequestContext();
        if(!StringUtils.isEmpty(uidx)) {
            UserResponse userResponse = userService.getUserDetail(uidx);

            if (userResponse != null && StringUtils.isNotBlank(userResponse.getEmailId())) {
                return Optional.of(userResponse.getEmailId());
            }
        }
        return Optional.of("system");
	}
}