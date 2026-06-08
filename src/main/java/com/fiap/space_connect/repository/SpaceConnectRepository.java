package com.fiap.space_connect.repository;

import com.fiap.space_connect.model.SpaceObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceConnectRepository extends JpaRepository<SpaceObject, Long> {

}
