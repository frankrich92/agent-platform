package com.htam.agent.common.vo;

import com.htam.agent.common.config.SerializableEnable;
import com.htam.agent.common.enums.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账号VO
 *
 * @author huxuehao
 */
@Data
@EqualsAndHashCode
public class AccountVO implements SerializableEnable {
    private Long id;
    private String nickname;
    private String email;
    private String username;
    private Boolean enabled;
    private List<Role> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
