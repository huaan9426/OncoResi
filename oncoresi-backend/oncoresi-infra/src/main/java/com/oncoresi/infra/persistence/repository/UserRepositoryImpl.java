package com.oncoresi.infra.persistence.repository;

import com.oncoresi.domain.entity.User;
import com.oncoresi.domain.repository.UserRepository;
import com.oncoresi.infra.persistence.converter.UserConverter;
import com.oncoresi.infra.persistence.jpa.UserJpaRepository;
import com.oncoresi.infra.persistence.po.UserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username)
                .map(UserConverter::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(UserConverter::toDomain);
    }

    @Override
    public User save(User user) {
        UserPO po = UserConverter.toPO(user);
        UserPO saved = userJpaRepository.save(po);
        return UserConverter.toDomain(saved);
    }
}
