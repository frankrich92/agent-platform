package com.htam.agent.capability.connector;

import java.util.List;
import java.util.Optional;

public interface ConnectorCatalog {

    List<EnterpriseConnectorDescriptor> listEnabled();

    Optional<EnterpriseConnectorDescriptor> findById(String connectorId);
}
