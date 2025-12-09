package tn.fst.eventsproject.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.fst.eventsproject.entities.Event;
import tn.fst.eventsproject.entities.Logistics;
import tn.fst.eventsproject.entities.Participant;
import tn.fst.eventsproject.entities.Tache;
import tn.fst.eventsproject.repositories.EventRepository;
import tn.fst.eventsproject.repositories.LogisticsRepository;
import tn.fst.eventsproject.repositories.ParticipantRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServicesImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    private Event event;
    private Participant participant;
    private Logistics logistics;

    @BeforeEach
    void setUp() {
        // Initialisation d'un événement
        event = new Event();
        event.setIdEvent(1);
        event.setDescription("Conference Tech 2024");
        event.setDateDebut(LocalDate.of(2024, 6, 1));
        event.setDateFin(LocalDate.of(2024, 6, 3));
        event.setCout(0.0f);
        event.setParticipants(new HashSet<>());
        event.setLogistics(new HashSet<>());

        // Initialisation d'un participant
        participant = new Participant();
        participant.setIdPart(1);
        participant.setNom("Tounsi");
        participant.setPrenom("Ahmed");
        participant.setTache(Tache.ORGANISATEUR);
        participant.setEvents(new HashSet<>());

        // Initialisation de logistics
        logistics = new Logistics();
        logistics.setIdLog(1);
        logistics.setDescription("Catering Service");
        logistics.setReserve(true);
        logistics.setPrixUnit(50.0f);
        logistics.setQuantite(100);
    }

    @Test
    void testAddParticipant_Success() {
        // Given
        when(participantRepository.save(any(Participant.class))).thenReturn(participant);

        // When
        Participant result = eventServices.addParticipant(participant);

        // Then
        assertNotNull(result);
        assertEquals("Tounsi", result.getNom());
        assertEquals("Ahmed", result.getPrenom());
        assertEquals(Tache.ORGANISATEUR, result.getTache());
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    void testAddAffectEvenParticipant_WithIdParticipant_NewEvents() {
        // Given - Participant sans événements existants
        participant.setEvents(null);
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        Event result = eventServices.addAffectEvenParticipant(event, 1);

        // Then
        assertNotNull(result);
        assertNotNull(participant.getEvents());
        assertTrue(participant.getEvents().contains(event));
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectEvenParticipant_WithIdParticipant_ExistingEvents() {
        // Given - Participant avec des événements existants
        Set<Event> existingEvents = new HashSet<>();
        Event oldEvent = new Event();
        oldEvent.setIdEvent(99);
        existingEvents.add(oldEvent);
        participant.setEvents(existingEvents);
        
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        Event result = eventServices.addAffectEvenParticipant(event, 1);

        // Then
        assertNotNull(result);
        assertEquals(2, participant.getEvents().size());
        assertTrue(participant.getEvents().contains(event));
        assertTrue(participant.getEvents().contains(oldEvent));
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectEvenParticipant_WithoutIdParticipant_MultipleParticipants() {
        // Given
        Participant participant1 = new Participant();
        participant1.setIdPart(1);
        participant1.setNom("Ben Ali");
        participant1.setPrenom("Fatma");
        participant1.setTache(Tache.INVITE);
        participant1.setEvents(null);

        Participant participant2 = new Participant();
        participant2.setIdPart(2);
        participant2.setNom("Mokhtar");
        participant2.setPrenom("Salma");
        participant2.setTache(Tache.ANIMATEUR);
        participant2.setEvents(new HashSet<>());

        Set<Participant> participants = new HashSet<>();
        participants.add(participant1);
        participants.add(participant2);
        event.setParticipants(participants);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant1));
        when(participantRepository.findById(2)).thenReturn(Optional.of(participant2));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        Event result = eventServices.addAffectEvenParticipant(event);

        // Then
        assertNotNull(result);
        assertNotNull(participant1.getEvents());
        assertTrue(participant1.getEvents().contains(event));
        assertTrue(participant2.getEvents().contains(event));
        verify(participantRepository, times(1)).findById(1);
        verify(participantRepository, times(1)).findById(2);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectLog_WithNewLogistics() {
        // Given
        event.setLogistics(null);
        when(eventRepository.findByDescription("Conference Tech 2024")).thenReturn(event);
        when(logisticsRepository.save(any(Logistics.class))).thenReturn(logistics);
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        Logistics result = eventServices.addAffectLog(logistics, "Conference Tech 2024");

        // Then
        assertNotNull(result);
        assertNotNull(event.getLogistics());
        assertTrue(event.getLogistics().contains(logistics));
        verify(eventRepository, times(1)).findByDescription("Conference Tech 2024");
        verify(eventRepository, times(1)).save(event);
        verify(logisticsRepository, times(1)).save(logistics);
    }

    @Test
    void testAddAffectLog_WithExistingLogistics() {
        // Given
        Set<Logistics> existingLogistics = new HashSet<>();
        Logistics oldLog = new Logistics();
        oldLog.setIdLog(99);
        existingLogistics.add(oldLog);
        event.setLogistics(existingLogistics);

        when(eventRepository.findByDescription("Conference Tech 2024")).thenReturn(event);
        when(logisticsRepository.save(any(Logistics.class))).thenReturn(logistics);

        // When
        Logistics result = eventServices.addAffectLog(logistics, "Conference Tech 2024");

        // Then
        assertNotNull(result);
        assertEquals(2, event.getLogistics().size());
        assertTrue(event.getLogistics().contains(logistics));
        assertTrue(event.getLogistics().contains(oldLog));
        verify(logisticsRepository, times(1)).save(logistics);
    }

    @Test
    void testGetLogisticsDates_WithReservedLogistics() {
        // Given
        LocalDate dateDebut = LocalDate.of(2024, 6, 1);
        LocalDate dateFin = LocalDate.of(2024, 6, 30);

        Logistics log1 = new Logistics();
        log1.setIdLog(1);
        log1.setReserve(true);

        Logistics log2 = new Logistics();
        log2.setIdLog(2);
        log2.setReserve(true);

        Set<Logistics> logisticsSet = new HashSet<>();
        logisticsSet.add(log1);
        logisticsSet.add(log2);
        event.setLogistics(logisticsSet);

        List<Event> events = Collections.singletonList(event);
        when(eventRepository.findByDateDebutBetween(dateDebut, dateFin)).thenReturn(events);

        // When
        List<Logistics> result = eventServices.getLogisticsDates(dateDebut, dateFin);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(log1));
        assertTrue(result.contains(log2));
        verify(eventRepository, times(1)).findByDateDebutBetween(dateDebut, dateFin);
    }

    @Test
    void testGetLogisticsDates_WithMixedReservation() {
        // Given
        LocalDate dateDebut = LocalDate.of(2024, 6, 1);
        LocalDate dateFin = LocalDate.of(2024, 6, 30);

        Logistics log1 = new Logistics();
        log1.setIdLog(1);
        log1.setReserve(true); // Réservé

        Logistics log2 = new Logistics();
        log2.setIdLog(2);
        log2.setReserve(false); // Non réservé

        Set<Logistics> logisticsSet = new HashSet<>();
        logisticsSet.add(log1);
        logisticsSet.add(log2);
        event.setLogistics(logisticsSet);

        List<Event> events = Collections.singletonList(event);
        when(eventRepository.findByDateDebutBetween(dateDebut, dateFin)).thenReturn(events);

        // When
        List<Logistics> result = eventServices.getLogisticsDates(dateDebut, dateFin);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(log1));
        assertFalse(result.contains(log2));
    }

    @Test
    void testGetLogisticsDates_EmptyLogistics() {
        // Given
        LocalDate dateDebut = LocalDate.of(2024, 6, 1);
        LocalDate dateFin = LocalDate.of(2024, 6, 30);

        event.setLogistics(new HashSet<>());
        List<Event> events = Collections.singletonList(event);
        when(eventRepository.findByDateDebutBetween(dateDebut, dateFin)).thenReturn(events);

        // When
        List<Logistics> result = eventServices.getLogisticsDates(dateDebut, dateFin);

        // Then
        assertNull(result);
        verify(eventRepository, times(1)).findByDateDebutBetween(dateDebut, dateFin);
    }

    @Test
    void testCalculCout_SingleEvent() {
        // Given
        Logistics log1 = new Logistics();
        log1.setPrixUnit(50.0f);
        log1.setQuantite(10);
        log1.setReserve(true);

        Logistics log2 = new Logistics();
        log2.setPrixUnit(100.0f);
        log2.setQuantite(5);
        log2.setReserve(true);

        Set<Logistics> logisticsSet = new HashSet<>();
        logisticsSet.add(log1);
        logisticsSet.add(log2);
        event.setLogistics(logisticsSet);

        List<Event> events = Collections.singletonList(event);
        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
            "Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(events);
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        eventServices.calculCout();

        // Then
        verify(eventRepository, times(1)).findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
            "Tounsi", "Ahmed", Tache.ORGANISATEUR);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testCalculCout_MultipleEvents() {
        // Given
        Event event1 = new Event();
        event1.setIdEvent(1);
        event1.setDescription("Event 1");
        
        Logistics log1 = new Logistics();
        log1.setPrixUnit(50.0f);
        log1.setQuantite(10);
        log1.setReserve(true);
        
        Set<Logistics> logSet1 = new HashSet<>();
        logSet1.add(log1);
        event1.setLogistics(logSet1);

        Event event2 = new Event();
        event2.setIdEvent(2);
        event2.setDescription("Event 2");
        
        Logistics log2 = new Logistics();
        log2.setPrixUnit(100.0f);
        log2.setQuantite(5);
        log2.setReserve(true);
        
        Set<Logistics> logSet2 = new HashSet<>();
        logSet2.add(log2);
        event2.setLogistics(logSet2);

        List<Event> events = Arrays.asList(event1, event2);
        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
            "Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(events);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventServices.calculCout();

        // Then
        verify(eventRepository, times(2)).save(any(Event.class));
    }

    @Test
    void testCalculCout_WithNonReservedLogistics() {
        // Given
        Logistics log1 = new Logistics();
        log1.setPrixUnit(50.0f);
        log1.setQuantite(10);
        log1.setReserve(true); // Réservé

        Logistics log2 = new Logistics();
        log2.setPrixUnit(100.0f);
        log2.setQuantite(5);
        log2.setReserve(false); // Non réservé - ne doit pas être compté

        Set<Logistics> logisticsSet = new HashSet<>();
        logisticsSet.add(log1);
        logisticsSet.add(log2);
        event.setLogistics(logisticsSet);

        List<Event> events = Collections.singletonList(event);
        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
            "Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(events);
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        eventServices.calculCout();

        // Then
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testCalculCout_EmptyEventsList() {
        // Given
        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
            "Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(Collections.emptyList());

        // When
        eventServices.calculCout();

        // Then
        verify(eventRepository, times(1)).findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
            "Tounsi", "Ahmed", Tache.ORGANISATEUR);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void testGetLogisticsDates_NoEventsFound() {
        // Given
        LocalDate dateDebut = LocalDate.of(2025, 1, 1);
        LocalDate dateFin = LocalDate.of(2025, 12, 31);
        
        when(eventRepository.findByDateDebutBetween(dateDebut, dateFin))
            .thenReturn(Collections.emptyList());

        // When
        List<Logistics> result = eventServices.getLogisticsDates(dateDebut, dateFin);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(eventRepository, times(1)).findByDateDebutBetween(dateDebut, dateFin);
    }
}
