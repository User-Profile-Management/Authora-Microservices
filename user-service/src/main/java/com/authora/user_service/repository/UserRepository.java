package com.authora.user_service.repository;

import com.authora.user_service.modal.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;
import com.authora.user_service.modal.User; // ✅ Use your actual model package

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Integer countByRole(Role role);

    @Query("SELECT u FROM User u JOIN u.role r WHERE UPPER(r.roleName) = UPPER(:roleName) AND u.status = :status")
    List<User> findByRoleAndStatus(@Param("roleName") String roleName, @Param("status") User.Status status);



    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.deletedAt IS NULL")
    Optional<User> findById(String userId);
    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> findByIdIncludingDeleted(@Param("userId") String userId);



    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE UPPER(u.role.roleName) = UPPER(:roleName) AND u.status = :status")
    Long countByRoleAndStatus(@Param("roleName") String roleName, @Param("status") User.Status status);

    List<User> findAllByDeletedAtIsNull();

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.deletedAt IS NULL")
    Optional<User> findByUserIdAndDeletedAtIsNull(String userId);

    @Query("SELECT u FROM User u WHERE u.status = :status AND u.deletedAt IS NULL")
    List<User> findByStatusAndDeletedAtIsNull(User.Status status);

    List<User> findByRoleAndStatus(Role role, User.Status status);
    List<User> findByRole(Role role);
    List<User> findByStatus(User.Status status);

    User findByUserId(String loggedInUserId);

    boolean existsByEmail(String email);

    boolean existsByContactNo(String contactNo);

    Integer countByRoleAndDeletedAtIsNull(Role role);

    boolean existsByUserId(String userId);
}

