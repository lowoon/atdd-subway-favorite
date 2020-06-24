package wooteco.subway.domain.line;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LineStationRepository extends JpaRepository<LineStation, Long> {
    void deleteByLineIdAndStationId(Long lineId, Long stationId);

    void deleteAllByStationId(Long stationId);
}
