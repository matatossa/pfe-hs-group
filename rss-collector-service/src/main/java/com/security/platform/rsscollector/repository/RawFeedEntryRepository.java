package com.security.platform.rsscollector.repository;

import com.security.platform.rsscollector.entity.RawFeedEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RawFeedEntryRepository extends JpaRepository<RawFeedEntry, Long> {

    boolean existsByUrl(String url);

    List<RawFeedEntry> findByProcessedFalse();

    List<RawFeedEntry> findByFeedSourceIdOrderByFetchedAtDesc(Integer feedSourceId);
}
