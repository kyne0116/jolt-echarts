package com.example.chart.repository;

import java.util.List;
import java.util.Optional;

/**
 * 通用模板仓储接口（模拟ORM Repository风格）
 */
public interface UniversalTemplateRepository<T, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    T save(T entity);
    void deleteById(ID id);
    boolean existsById(ID id);
}

