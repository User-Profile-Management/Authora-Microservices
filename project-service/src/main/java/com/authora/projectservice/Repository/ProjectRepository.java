package com.authora.projectservice.Repository;

import com.authora.projectservice.DTO.ProjectDTO;
import com.authora.projectservice.Entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {

    // Method to check if a project exists by its name
    boolean existsByProjectName(String projectName);

    // Method to find a project by its name
    Optional<Project> findByProjectName(String projectName);

    // Method to find all projects that are not deleted
    List<Project> findByDeletedAtIsNull();

    // Method to find projects assigned to a specific mentor
    List<Project> findProjectsByMentorId(String mentorId);

    // Custom method to find projects by a mentor's ID and not deleted
    List<Project> findByMentorIdAndDeletedAtIsNull(String mentorId);

    // Method to find a project by its ID
    Optional<Project> findById(Integer projectId);

    // Method to find a project by its name and check if mentor is linked
    Optional<Project> findByProjectNameAndMentorId(String projectName, String mentorId);

    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NULL")
    List<Project> findAllProjects();

    boolean existsByMentorIdAndProjectId(String mentorId, Integer projectId);

    @Query("SELECT p FROM Project p WHERE p.projectId IN (SELECT up.projectId FROM UserProject up WHERE up.userId = :userId)")
    List<Project> findProjectsByUserId(@Param("userId") String userId);
}
