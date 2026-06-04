package com.htam.agent.worker.file.service;

import com.htam.agent.common.entity.StorageProtocol;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.repo.file.StorageProtocolRepository;
import com.htam.agent.repo.support.RepoPage;
import com.htam.agent.worker.file.storage.core.FileStorageService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageProtocolServiceImplTest {

    @Test
    void createsDefaultLocalProtocolWhenNoValidStorageExists() throws Exception {
        InMemoryStorageProtocolRepository repository = new InMemoryStorageProtocolRepository();
        StorageProtocolServiceImpl service = new StorageProtocolServiceImpl(repository);
        Path localDir = Files.createTempDirectory("agent-storage-test");
        setDefaultLocalStorageDir(service, localDir.toString());

        FileStorageService storageService = service.getStorageService();

        assertEquals("LOCAL", storageService.getProtocol());
        List<StorageProtocol> validProtocols = repository.listCurrentValid();
        assertEquals(1, validProtocols.size());
        StorageProtocol protocol = validProtocols.getFirst();
        assertEquals("default-local-storage", protocol.getName());
        assertEquals("LOCAL", protocol.getProtocol());
        assertEquals(1, protocol.getValid());
        assertNotNull(protocol.getProtocolConfig());
    }

    @Test
    void reportsClearErrorWhenMultipleValidProtocolsExist() {
        InMemoryStorageProtocolRepository repository = new InMemoryStorageProtocolRepository();
        repository.save(protocol("local-a"));
        repository.save(protocol("local-b"));
        StorageProtocolServiceImpl service = new StorageProtocolServiceImpl(repository);

        RuntimeException exception = assertThrows(RuntimeException.class, service::getStorageService);

        assertEquals("存储配置不存在唯一一个有效的配置", exception.getMessage());
    }

    private static StorageProtocol protocol(String name) {
        StorageProtocol protocol = new StorageProtocol();
        protocol.setName(name);
        protocol.setProtocol("LOCAL");
        protocol.setProtocolConfig("{}");
        protocol.setValid(1);
        return protocol;
    }

    private static void setDefaultLocalStorageDir(StorageProtocolServiceImpl service, String localDir) throws Exception {
        Field field = StorageProtocolServiceImpl.class.getDeclaredField("defaultLocalStorageDir");
        field.setAccessible(true);
        field.set(service, localDir);
    }

    private static final class InMemoryStorageProtocolRepository implements StorageProtocolRepository {
        private final List<StorageProtocol> protocols = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public RepoPage<StorageProtocol> page(PageParams pageParams, String name, String protocol, Integer valid) {
            return new RepoPage<>(List.copyOf(protocols), protocols.size(), protocols.size(), 1);
        }

        @Override
        public StorageProtocol getById(Long id) {
            return protocols.stream()
                    .filter(protocol -> protocol.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<StorageProtocol> listValidExcludeId(Long id) {
            return protocols.stream()
                    .filter(protocol -> Integer.valueOf(1).equals(protocol.getValid()))
                    .filter(protocol -> id == null || !id.equals(protocol.getId()))
                    .toList();
        }

        @Override
        public List<StorageProtocol> listCurrentValid() {
            return protocols.stream()
                    .filter(protocol -> Integer.valueOf(1).equals(protocol.getValid()))
                    .toList();
        }

        @Override
        public boolean save(StorageProtocol body) {
            if (body.getId() == null) {
                body.setId(nextId++);
            }
            protocols.add(body);
            return true;
        }

        @Override
        public boolean updateMainFields(StorageProtocol body, boolean clearProtocolConfig) {
            return false;
        }

        @Override
        public boolean setAllInvalid() {
            protocols.forEach(protocol -> protocol.setValid(0));
            return true;
        }

        @Override
        public boolean setValid(Long id) {
            StorageProtocol protocol = getById(id);
            if (protocol == null) {
                return false;
            }
            protocol.setValid(1);
            return true;
        }

        @Override
        public boolean updateProtocolConfig(Long id, String protocolConfig) {
            StorageProtocol protocol = getById(id);
            if (protocol == null) {
                return false;
            }
            protocol.setProtocolConfig(protocolConfig);
            return true;
        }

        @Override
        public boolean deleteByIds(List<Long> ids) {
            return protocols.removeIf(protocol -> ids.contains(protocol.getId()));
        }
    }
}
