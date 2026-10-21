package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManagerService {

    private static final Logger logger = LoggerFactory.getLogger(ManagerService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Fetches team members managed by managerId.
     * Caches the result in Redis (or in-memory cache) with cache name 'team' and key #managerId.
     * Subsequent requests for the same managerId return immediately from cache without querying PostgreSQL.
     */
    @Cacheable(value = "team", key = "#managerId")
    public List<User> getTeamByManagerId(Long managerId) {
        logger.info(">>> [CACHE MISS] Fetching team members for manager ID {} from PostgreSQL database...", managerId);
        return userRepository.findByManagerId(managerId);
    }

    /**
     * Evicts specific manager's team cache when an employee is registered, updated, or reassigned.
     */
    @CacheEvict(value = "team", key = "#managerId")
    public void evictTeamCache(Long managerId) {
        logger.info(">>> [CACHE EVICT] Evicted team cache for manager ID: {}", managerId);
    }

    /**
     * Evicts all entries in the 'team' cache.
     */
    @CacheEvict(value = "team", allEntries = true)
    public void evictAllTeamCaches() {
        logger.info(">>> [CACHE EVICT] Evicted all entries in 'team' cache.");
    }
}
