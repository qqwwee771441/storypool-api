package com.wudc.storypool.domain.user.repository;

import com.wudc.storypool.domain.user.controller.response.UserProfileResponse;
import com.wudc.storypool.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query("""
SELECT new com.wudc.storypool.domain.user.controller.response.UserProfileResponse(
    u.email,
    u.nickname,
    u.profileImageUrl,
    u.description
) FROM User u
WHERE u.id = :userId
""")
    Optional<UserProfileResponse> findMyProfileById(String userId);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);

    @Query(value = "SELECT * FROM user u WHERE u.email = :email AND u.deleted = true", nativeQuery = true)
    Optional<User> findDeletedUserByEmail(String email);
}
