package com.htam.agent.common.message;

import com.htam.agent.common.enums.Role;

public interface AccountRoleChangePublisher {

    void publishRoleChanged(String accountId, Role role);
}
