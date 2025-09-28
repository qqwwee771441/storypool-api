package com.wudc.storypool.domain.fairytale.repository;

import com.wudc.storypool.domain.fairytale.entity.FairytaleePage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FairytalePageRepository extends JpaRepository<FairytaleePage, String> {
    
    List<FairytaleePage> findByFairytaleIdOrderByPageIndexAsc(String fairytaleId);
    
    void deleteByFairytaleId(String fairytaleId);
}