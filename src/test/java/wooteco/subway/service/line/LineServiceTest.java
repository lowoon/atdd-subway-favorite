package wooteco.subway.service.line;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wooteco.subway.domain.line.Line;
import wooteco.subway.domain.line.LineRepository;
import wooteco.subway.domain.line.LineStation;
import wooteco.subway.domain.station.Station;
import wooteco.subway.service.line.dto.LineStationCreateRequest;

@ExtendWith(MockitoExtension.class)
public class LineServiceTest {
    private static final String STATION_NAME1 = "강남역";
    private static final String STATION_NAME2 = "역삼역";
    private static final String STATION_NAME3 = "선릉역";
    private static final String STATION_NAME4 = "삼성역";

    @Mock
    private LineRepository lineRepository;
    @Mock
    private LineStationService lineStationService;

    private LineService lineService;

    private Line line;
    private Station station1;
    private Station station2;
    private Station station3;
    private Station station4;

    @BeforeEach
    void setUp() {
        lineService = new LineService(lineStationService, lineRepository);

        station1 = new Station(1L, STATION_NAME1);
        station2 = new Station(2L, STATION_NAME2);
        station3 = new Station(3L, STATION_NAME3);
        station4 = new Station(4L, STATION_NAME4);

        line = new Line(1L, "2호선", LocalTime.of(05, 30), LocalTime.of(22, 30), 5);
        line.addLineStation(new LineStation(1L, line, null, station1, 10, 10));
        line.addLineStation(new LineStation(2L, line, station1, station2, 10, 10));
        line.addLineStation(new LineStation(3L, line, station2, station3, 10, 10));
    }

    @Test
    void addLineStationAtTheFirstOfLine() {
        when(lineRepository.findById(line.getId())).thenReturn(Optional.of(line));
        when(lineStationService.createLineStation(null, station4.getId(), 10, 10))
            .thenReturn(new LineStation(null, station4, 10, 10));
        when(lineStationService.save(any())).thenReturn(
            new LineStation(4L, line, null, station4, 10, 10));
        LineStationCreateRequest request = new LineStationCreateRequest(null, station4.getId(), 10,
            10);
        lineService.addLineStation(line.getId(), request);

        assertThat(line.getLineStations().getValues()).hasSize(4);

        List<Long> stationIds = line.getLineStationIds();
        assertThat(stationIds.get(0)).isEqualTo(4L);
        assertThat(stationIds.get(1)).isEqualTo(1L);
        assertThat(stationIds.get(2)).isEqualTo(2L);
        assertThat(stationIds.get(3)).isEqualTo(3L);
    }

    @Test
    void addLineStationBetweenTwo() {
        when(lineRepository.findById(line.getId())).thenReturn(Optional.of(line));
        when(lineStationService.createLineStation(station1.getId(), station4.getId(), 10, 10))
            .thenReturn(new LineStation(station1, station4, 10, 10));
        when(lineStationService.save(any())).thenReturn(
            new LineStation(4L, line, station1, station4, 10, 10));
        LineStationCreateRequest request = new LineStationCreateRequest(station1.getId(),
            station4.getId(), 10, 10);
        lineService.addLineStation(line.getId(), request);

        assertThat(line.getLineStations().getValues()).hasSize(4);

        List<Long> stationIds = line.getLineStationIds();
        assertThat(stationIds.get(0)).isEqualTo(1L);
        assertThat(stationIds.get(1)).isEqualTo(4L);
        assertThat(stationIds.get(2)).isEqualTo(2L);
        assertThat(stationIds.get(3)).isEqualTo(3L);
    }

    @Test
    void addLineStationAtTheEndOfLine() {
        when(lineRepository.findById(line.getId())).thenReturn(Optional.of(line));
        when(lineStationService.createLineStation(station3.getId(), station4.getId(), 10, 10))
            .thenReturn(new LineStation(station3, station4, 10, 10));
        when(lineStationService.save(any())).thenReturn(
            new LineStation(4L, line, station3, station4, 10, 10));
        LineStationCreateRequest request = new LineStationCreateRequest(station3.getId(),
            station4.getId(), 10, 10);
        lineService.addLineStation(line.getId(), request);

        assertThat(line.getLineStations().getValues()).hasSize(4);

        List<Long> stationIds = line.getLineStationIds();
        assertThat(stationIds.get(0)).isEqualTo(1L);
        assertThat(stationIds.get(1)).isEqualTo(2L);
        assertThat(stationIds.get(2)).isEqualTo(3L);
        assertThat(stationIds.get(3)).isEqualTo(4L);
    }

    @Test
    void removeLineStationAtTheFirstOfLine() {
        when(lineRepository.findById(line.getId())).thenReturn(Optional.of(line));
        lineService.removeLineStation(line.getId(), 1L);

        assertThat(line.getLineStations().getValues()).hasSize(2);

        List<Long> stationIds = line.getLineStationIds();
        assertThat(stationIds.get(0)).isEqualTo(2L);
        assertThat(stationIds.get(1)).isEqualTo(3L);
    }

    @Test
    void removeLineStationBetweenTwo() {
        when(lineRepository.findById(line.getId())).thenReturn(Optional.of(line));
        lineService.removeLineStation(line.getId(), 2L);

        verify(lineRepository).save(any());
    }

    @Test
    void removeLineStationAtTheEndOfLine() {
        when(lineRepository.findById(line.getId())).thenReturn(Optional.of(line));
        lineService.removeLineStation(line.getId(), 3L);

        assertThat(line.getLineStations().getValues()).hasSize(2);

        List<Long> stationIds = line.getLineStationIds();
        assertThat(stationIds.get(0)).isEqualTo(1L);
        assertThat(stationIds.get(1)).isEqualTo(2L);
    }

    @Test
    void removeOtherLineHasLineStation() {
        Line newLine = new Line(2L, "신분당선", LocalTime.of(05, 30), LocalTime.of(22, 30), 5);

        when(lineRepository.findById(1L)).thenReturn(Optional.of(line));
        lineService.removeLineStation(1L, 3L);

        when(lineRepository.findById(2L)).thenReturn(Optional.of(newLine));

        assertThat(lineRepository.findById(2L)
            .orElseThrow(AssertionError::new)
            .getLineStations()
            .getValues()).hasSize(0);
    }
}
