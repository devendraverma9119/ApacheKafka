package com.learnkafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.jpa.LibraryEventsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class LibraryEventsService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    LibraryEventsRepository libraryEventsRepository;

    public void processLibraryEvent(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("libraryEvent : {}",libraryEvent);

        switch ((libraryEvent.getLibraryEventType())){
            case NEW:
                save(libraryEvent);
                break;
            case UPDATE:
                update(libraryEvent);
                break;
            default:
                log.info("invalid Library Event Type");
        }
    }

    private void update(LibraryEvent libraryEvent) {
        if(libraryEvent.getLibraryEventId()==null){
            throw new IllegalArgumentException("Library Event Id is missing");
        }
        Optional<LibraryEvent> libraryEventOptional = libraryEventsRepository.findById(libraryEvent.getLibraryEventId());
        if(!libraryEventOptional.isPresent()){
            throw new IllegalArgumentException("Library Event is not valid ");
        }
        log.info("validation successful for library event : {}",libraryEvent.getLibraryEventId());
        libraryEventsRepository.save(libraryEvent);
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);
        log.info("successfully persisted the library event {} ",libraryEvent);
    }
}
