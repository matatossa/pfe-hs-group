package com.security.platform.rsscollector.repository;

import com.security.platform.rsscollector.entity.FeedSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedSourceRepository extends JpaRepository<FeedSource, Integer> {

    List<FeedSource> findByActiveTrue();
}
