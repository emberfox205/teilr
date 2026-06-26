package frauas.teilr.repository;

import frauas.teilr.entity.Group;
import org.springframework.data.repository.CrudRepository;

public interface GroupRepository extends CrudRepository<Group, Long>{
    // Basic CRUD inherited from CrudRepository.
    // Add custom queries here as features grow.
}
