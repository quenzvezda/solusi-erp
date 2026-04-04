package com.solusi.erp.common.news.infrastructure.adapter;

import com.solusi.erp.common.news.domain.model.News;
import com.solusi.erp.common.news.domain.model.NewsStatus;
import com.solusi.erp.common.news.domain.repository.NewsRepository;
import com.solusi.erp.common.news.infrastructure.persistence.NewsEntity;
import com.solusi.erp.common.news.infrastructure.persistence.NewsJpaRepository;
import com.solusi.erp.common.news.infrastructure.persistence.NewsPersistenceMapper;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.core.infrastructure.util.PageableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository Adapter (Infrastructure Layer).
 * Mengimplementasikan kontrak Repository dari Domain menggunakan JPA.
 */
@Component
@RequiredArgsConstructor
public class NewsRepositoryAdapter implements NewsRepository {

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
    public Optional<News> findByTitle(String title) {
        return jpaRepository.findByTitle(title).map(mapper::toDomain);
    }

    @Override
    public List<News> findPublishedNews() {
        return jpaRepository.findAllByStatus(NewsStatus.PUBLISHED)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<News> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<News> findAll(String keyword, Pageable pageable) {
        org.springframework.data.domain.Pageable springPageable = PageableMapper.toSpring(pageable);
        org.springframework.data.domain.Page<NewsEntity> entityPage =
                jpaRepository.findAllByKeyword(keyword, springPageable);
        List<News> content = entityPage.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
        return new Page<>(content, entityPage.getNumber(), entityPage.getSize(), entityPage.getTotalElements());
    }
}
