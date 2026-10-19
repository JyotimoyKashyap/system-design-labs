package com.jyotimoykashyap.splitwiselld.repository;

import com.jyotimoykashyap.splitwiselld.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {

}
