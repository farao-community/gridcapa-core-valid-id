package com.farao_community.farao.gridcapa_core_valid_intraday.app.repositories;

import com.farao_community.farao.gridcapa_core_valid_intraday.app.entities.NetPositionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NetPositionHistoryRepository extends JpaRepository<NetPositionHistory, UUID> {
}
