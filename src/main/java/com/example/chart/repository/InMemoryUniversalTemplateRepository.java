package com.example.chart.repository;

import com.example.chart.repository.model.UniversalTemplateEntity;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存实现：使用Map模拟数据库表
 */
@Repository
public class InMemoryUniversalTemplateRepository implements UniversalTemplateRepository<UniversalTemplateEntity, String> {

    // key: chartId
    private final Map<String, UniversalTemplateEntity> storage = new ConcurrentHashMap<>();

    @Override
    public List<UniversalTemplateEntity> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<UniversalTemplateEntity> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public UniversalTemplateEntity save(UniversalTemplateEntity entity) {
        storage.put(entity.getChartId(), entity);
        return entity;
    }

    @Override
    public void deleteById(String id) {
        storage.remove(id);
    }

    @Override
    public boolean existsById(String id) {
        return storage.containsKey(id);
    }
}

