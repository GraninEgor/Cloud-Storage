package org.example.cloudstorage.database.repository;

import org.example.cloudstorage.database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u where  u.username = :username")
    boolean existsByUsername(@Param("username") String username);
}
