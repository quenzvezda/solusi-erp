package com.solusi.erp.common.news.adapters.gateway;

import com.solusi.erp.common.news.adapters.mapper.NewsPersistenceMapper;
import com.solusi.erp.common.news.entities.News;
import com.solusi.erp.common.news.entities.NewsStatus;
import com.solusi.erp.common.news.frameworks.persistence.NewsEntity;
import com.solusi.erp.common.news.frameworks.persistence.NewsJpaRepository;
import com.solusi.erp.common.news.usecases.NewsOutputBoundary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Clean Architecture: Database Gateway Implementation.
 * Mengimplementasikan Output Boundary.
 */
@Component
@RequiredArgsConstructor
public class NewsPersistenceGateway implements NewsOutputBoundary {

    private final NewsJpaRepository jpaRepository;
    private final NewsPersistenceMapper mapper;

    @Override
    public News save(News news) {
        NewsEntity entity = mapper.toEntity(news);
        NewsEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<News> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<News> findPublishedNews() {
        return jpaRepository.findAllByStatus(NewsStatus.PUBLISHED)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
