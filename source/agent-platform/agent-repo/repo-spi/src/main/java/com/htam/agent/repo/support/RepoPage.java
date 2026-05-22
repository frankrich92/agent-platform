package com.htam.agent.repo.support;

import java.util.List;

public record RepoPage<T>(List<T> records, long total, long size, long current) {
}
