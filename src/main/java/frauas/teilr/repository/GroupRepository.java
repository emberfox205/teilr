package frauas.teilr.repository;

import frauas.teilr.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    // Basic CRUD inherited from JpaRepository (findById, save, delete, findAll, etc.)
    // Add custom queries here as features grow.
}
