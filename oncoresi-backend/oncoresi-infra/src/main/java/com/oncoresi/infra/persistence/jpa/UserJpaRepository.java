package com.oncoresi.infra.persistence.jpa;

import com.oncoresi.infra.persistence.po.UserPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户JPA Repository
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserPO, Long> {

    /**
     * 根据用户名查询
     */
    Optional<UserPO> findByUsername(String username);
}
