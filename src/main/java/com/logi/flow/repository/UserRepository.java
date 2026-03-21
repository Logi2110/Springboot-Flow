package com.logi.flow.repository;

import com.logi.flow.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Layer — Spring Data JPA repository for UserEntity.
 *
 * Part of Data / Persistence Layer (Flow 11).
 *
 * Spring Data auto-generates the SQL + JDBC boilerplate at startup
 * based on the method names and @Query annotations here.
 *
 * All calls are automatically wrapped in a transaction when called from
 * a @Transactional service method — Hibernate flushes all pending changes
 * to the DB when the transaction commits.
 *
 * Demonstrated transaction scenarios (in UserTransactionService):
 *   REQUIRED (default) — join caller's transaction, or start a new one
 *   REQUIRES_NEW       — always start a brand-new transaction (suspends caller's)
 *   READ_ONLY          — hint to DB/Hibernate: no writes allowed, optimize reads
 *   ROLLBACK           — any RuntimeException automatically rolls back the transaction
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Derived query — Spring Data generates:
     *   SELECT * FROM users WHERE email = ?
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Derived query — Spring Data generates:
     *   SELECT * FROM users WHERE department = ?
     */
    List<UserEntity> findByDepartment(String department);

    /**
     * Derived query — Spring Data generates:
     *   SELECT * FROM users WHERE name ILIKE '%keyword%'
     * NOTE: containsIgnoreCase maps to LIKE in SQL — Hibernate adds wildcards.
     */
    List<UserEntity> findByNameContainingIgnoreCase(String keyword);

    /**
     * Explicit JPQL query — demonstrates @Query usage.
     * Selects users created after a given timestamp.
     */
    @Query("SELECT u FROM UserEntity u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<UserEntity> findRecentUsers(@Param("since") java.time.LocalDateTime since);

    /**
     * Derived query — Spring Data generates:
     *   SELECT COUNT(*) FROM users WHERE department = ?
     */
    long countByDepartment(String department);

    /**
     * Check email uniqueness before insert.
     */
    boolean existsByEmail(String email);
}
