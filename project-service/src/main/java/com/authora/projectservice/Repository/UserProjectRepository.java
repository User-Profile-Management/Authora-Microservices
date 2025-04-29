package com.authora.projectservice.Repository;

import com.authora.projectservice.Entity.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, Integer> {

    // Find all user-projects where deletedAt is null
    List<UserProject> findByDeletedAtIsNull();

    // Find user-project by userId and projectId where deletedAt is null
    Optional<UserProject> findByUserIdAndProjectIdAndDeletedAtIsNull(String userId, Integer projectId);

    // Find all user-projects by userId where deletedAt is null
    List<UserProject> findByUserIdAndDeletedAtIsNull(String userId);

    // Find by userId (including deleted)
    List<UserProject> findByUserId(String userId);

    // Find by projectId (including deleted)
    List<UserProject> findByProjectId(Integer projectId);

    // Find by userId and projectId (including deleted)
    Optional<UserProject> findByUserIdAndProjectId(String userId, Integer projectId);

    // Find by list of projectIds where deletedAt is null
    List<UserProject> findByProjectIdInAndDeletedAtIsNull(List<Integer> projectIds);

    // Custom query to find userIds by mentorId through the project table
    @Query("SELECT DISTINCT up.userId FROM UserProject up " +
            "JOIN Project p ON up.projectId = p.projectId " +
            "WHERE p.mentorId = :mentorId AND up.deletedAt IS NULL")
    List<String> findUserIdsByMentorId(@Param("mentorId") String mentorId);
}