package com.logi.flow.service;

import com.logi.flow.dto.UserRequest;
import com.logi.flow.dto.UserResponse;
import com.logi.flow.entity.UserEntity;
import com.logi.flow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Transaction Service — demonstrates all key @Transactional scenarios.
 *
 * Part of Data / Persistence Layer (Flow 11).
 *
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │  TRANSACTION FLOW (for every public method below):                          │
 * │                                                                              │
 * │  HTTP Request                                                                │
 * │       │                                                                      │
 * │       ▼                                                                      │
 * │  Spring AOP proxy wraps the bean at startup                                 │
 * │       │                                                                      │
 * │       ▼  @Transactional method called                                        │
 * │  PlatformTransactionManager.getTransaction()  → opens connection from pool  │
 * │       │  (or joins/suspends existing tx — depends on Propagation)           │
 * │       ▼                                                                      │
 * │  EntityManager (Hibernate Session) bound to current thread                  │
 * │       │                                                                      │
 * │       ▼  method body runs                                                    │
 * │  repository.save() / findBy*() — Hibernate tracks dirty entities            │
 * │       │                                                                      │
 * │       ▼  method returns normally                                             │
 * │  PlatformTransactionManager.commit()  → Hibernate flush → SQL → COMMIT      │
 * │       │  (or ROLLBACK if RuntimeException propagates out)                   │
 * │       ▼                                                                      │
 * │  Connection returned to pool                                                 │
 * └──────────────────────────────────────────────────────────────────────────────┘
 */
@Service
public class UserTransactionService {

    private static final Logger log = LoggerFactory.getLogger(UserTransactionService.class);

    @Autowired
    private UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────────────────────
    // 11a — CREATE  (Propagation.REQUIRED — default)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * FLOW 11a: Standard INSERT with REQUIRED propagation.
     *
     * Propagation.REQUIRED (default):
     *   • If a transaction already exists  → JOIN it.
     *   • If no transaction exists         → START a new one.
     *
     * What happens here:
     *   1. Spring AOP opens a new transaction (no caller tx).
     *   2. email uniqueness checked (SELECT).
     *   3. userRepository.save() → Hibernate queues INSERT.
     *   4. Method returns → Spring AOP commits → SQL INSERT fires → COMMIT.
     *
     * Throws DuplicateEmailException (unchecked) → Spring rolls back automatically.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserResponse createUser(UserRequest request) {
        log.info("💾 [TX - REQUIRED]  createUser() START — opening new transaction");

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("💾 [TX - REQUIRED]  email '{}' already exists — throwing (triggers ROLLBACK)", request.getEmail());
            throw new DuplicateEmailException("Email already registered: " + request.getEmail());
        }

        UserEntity entity = new UserEntity(request.getName(), request.getEmail(), request.getDepartment());
        UserEntity saved = userRepository.save(entity);

        log.info("💾 [TX - REQUIRED]  createUser() COMMIT — saved id={}", saved.getId());
        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 11b — READ  (readOnly = true)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * FLOW 11b: SELECT by id with READ-ONLY transaction.
     *
     * readOnly = true:
     *   • Hibernate skips dirty-checking on flush (no change tracking overhead).
     *   • PostgreSQL driver / connection pool may route to a read replica.
     *   • Attempting a write inside a readOnly transaction → exception at flush time.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("💾 [TX - READ_ONLY] getUserById() id={}", id);

        UserEntity entity = userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("User not found: id=" + id));

        log.info("💾 [TX - READ_ONLY] getUserById() FOUND — {}", entity);
        return toResponse(entity);
    }

    /**
     * FLOW 11b (list): SELECT all — same READ_ONLY semantics.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("💾 [TX - READ_ONLY] getAllUsers()");
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * FLOW 11b (find by dept): SELECT by department.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByDepartment(String department) {
        log.info("💾 [TX - READ_ONLY] getUsersByDepartment() dept='{}'", department);
        return userRepository.findByDepartment(department).stream().map(this::toResponse).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 11c — UPDATE  (Propagation.REQUIRED + optimistic dirty-check)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * FLOW 11c: UPDATE — demonstrates Hibernate dirty-checking.
     *
     * No explicit save() needed:
     *   Hibernate tracks the entity returned by findById() as a "managed" entity.
     *   When the transaction commits, Hibernate detects that fields changed and
     *   generates UPDATE SQL automatically — this is the "dirty-check" mechanism.
     *
     * If id not found → NoSuchElementException (unchecked) → automatic ROLLBACK.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserResponse updateUser(Long id, UserRequest request) {
        log.info("💾 [TX - REQUIRED]  updateUser() START id={}", id);

        UserEntity entity = userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("User not found: id=" + id));

        // Mutate the managed entity — Hibernate will generate UPDATE SQL on commit.
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        entity.setDepartment(request.getDepartment());

        // No save() call needed — Hibernate dirty-check handles it.
        log.info("💾 [TX - REQUIRED]  updateUser() COMMIT — dirty-check will generate UPDATE SQL");
        return toResponse(entity);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 11d — DELETE  (Propagation.REQUIRED)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * FLOW 11d: DELETE — finds entity then deletes within same transaction.
     *
     * repository.delete(entity) marks the entity for removal.
     * Hibernate generates DELETE SQL when the transaction commits.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteUser(Long id) {
        log.info("💾 [TX - REQUIRED]  deleteUser() START id={}", id);

        UserEntity entity = userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("User not found: id=" + id));

        userRepository.delete(entity);
        log.info("💾 [TX - REQUIRED]  deleteUser() COMMIT — DELETE SQL will fire on commit");
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 11e — ROLLBACK DEMO  (intentional failure to demonstrate automatic rollback)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * FLOW 11e: Rollback demo — inserts two users, then throws intentionally.
     *
     * Both INSERTs are queued in the same transaction.
     * The RuntimeException at the end causes Spring to call ROLLBACK.
     * Neither user ends up in the database — this is Atomicity (the A in ACID).
     *
     * Isolation.READ_COMMITTED:
     *   Other transactions cannot see un-committed data from this transaction.
     *   This is the default isolation level in PostgreSQL.
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void rollbackDemo(UserRequest user1, UserRequest user2) {
        log.info("💾 [TX - ROLLBACK_DEMO] START — will insert 2 users then force rollback");

        UserEntity e1 = userRepository.save(new UserEntity(user1.getName(), user1.getEmail(), user1.getDepartment()));
        log.info("💾 [TX - ROLLBACK_DEMO] queued INSERT for user1 id={} (not committed yet)", e1.getId());

        UserEntity e2 = userRepository.save(new UserEntity(user2.getName(), user2.getEmail(), user2.getDepartment()));
        log.info("💾 [TX - ROLLBACK_DEMO] queued INSERT for user2 id={} (not committed yet)", e2.getId());

        // Intentional failure — Spring AOP catches this unchecked exception,
        // calls transactionManager.rollback(), and re-throws.
        throw new RuntimeException("Intentional failure — both INSERTs will be rolled back (ACID: Atomicity)");
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 11f — REQUIRES_NEW  (always starts a brand-new independent transaction)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * FLOW 11f: REQUIRES_NEW — audit log written in its own transaction.
     *
     * Propagation.REQUIRES_NEW:
     *   • Suspends any existing transaction.
     *   • Opens a new, completely independent transaction.
     *   • Commits (or rolls back) independently of the caller.
     *
     * Common use-case: audit logs, outbox events — must be persisted even if the
     * outer business transaction rolls back.
     *
     * NOTE: self-calls within the same bean do NOT go through the AOP proxy,
     * so REQUIRES_NEW must be called from a DIFFERENT bean to take effect.
     * Here it is called from the controller, which satisfies that requirement.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserResponse createUserWithNewTx(UserRequest request) {
        log.info("💾 [TX - REQUIRES_NEW] createUserWithNewTx() START — brand-new independent transaction");

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already registered: " + request.getEmail());
        }

        UserEntity entity = userRepository.save(new UserEntity(request.getName(), request.getEmail(), request.getDepartment()));
        log.info("💾 [TX - REQUIRES_NEW] createUserWithNewTx() COMMIT id={}", entity.getId());
        return toResponse(entity);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────────────────────────

    private UserResponse toResponse(UserEntity e) {
        UserResponse r = new UserResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setEmail(e.getEmail());
        r.setDepartment(e.getDepartment());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Custom exceptions (unchecked — trigger automatic rollback by default)
    // ─────────────────────────────────────────────────────────────────────────────

    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String msg) { super(msg); }
    }
}
