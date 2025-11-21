package com.db.bank.repository;

import com.db.bank.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 1) userId로 유저 조회 (로그인, 내 정보 조회)
    Optional<User> findByUserId(String userId);

    // 2) userId 중복 여부 확인 (회원가입 아이디 중복 체크)
    boolean existsByUserId(String userId);

    // 3) 유저 + 계좌 목록을 한 번에 조회 (마이페이지: 내 계좌들)
    @Query("select distinct u from User u left join fetch u.accounts where u.id = :userId")
    Optional<User> findWithAccountsById(@Param("userId") Long userId);

    // 로그인용
    Optional<User> findByUserIdAndPassword(String userId, String password);
}
