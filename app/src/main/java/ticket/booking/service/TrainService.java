package ticket.booking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainService {

    private List<Train> trainList;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String TRAIN_DB_PATH = "app/src/main/resources/localDb/trains.json";

    public TrainService() throws IOException {
        trainList = objectMapper.readValue(new File(TRAIN_DB_PATH), new TypeReference<List<Train>>() {});
    }

    public List<Train> searchTrains(String source, String destination) {
        return trainList.stream()
                .filter(train -> validTrain(train, source, destination))
                .collect(Collectors.toList());
    }

    public void addTrain(Train newTrain) {
        Optional<Train> existing = trainList.stream()
                .filter(t -> Objects.equals(t.getTrainId(), newTrain.getTrainId()))
                .findFirst();
        if (existing.isPresent()) {
            updateTrain(newTrain);
        } else {
            trainList.add(newTrain);
            saveTrainListToFile();
        }
    }

    public void updateTrain(Train updatedTrain) {
        OptionalInt index = IntStream.range(0, trainList.size())
                .filter(i -> Objects.equals(trainList.get(i).getTrainId(), updatedTrain.getTrainId()))
                .findFirst();
        if (index.isPresent()) {
            trainList.set(index.getAsInt(), updatedTrain);
            saveTrainListToFile();
        } else {
            addTrain(updatedTrain);
        }
    }

    private void saveTrainListToFile() {
        try {
            objectMapper.writeValue(new File(TRAIN_DB_PATH), trainList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Train> getAllTrains() {
        return trainList;
    }

    private boolean validTrain(Train train, String source, String destination) {
        List<String> stations = train.getStations();
        if (stations == null || stations.isEmpty()) return false;
        int srcIdx = stations.indexOf(source.toLowerCase());
        int dstIdx = stations.indexOf(destination.toLowerCase());
        return srcIdx != -1 && dstIdx != -1 && srcIdx < dstIdx;
    }
}
