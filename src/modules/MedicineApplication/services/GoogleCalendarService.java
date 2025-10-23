package modules.MedicineApplication.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import modules.MedicineApplication.models.MedicineApplication;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarService {
    
    private static final String APPLICATION_NAME = System.getenv("GOOGLE_CALENDAR_APPLICATION_NAME") != null ? 
        System.getenv("GOOGLE_CALENDAR_APPLICATION_NAME") : "Pata Amiga";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String SERVICE_ACCOUNT_FILE_PATH = System.getenv("GOOGLE_SERVICE_ACCOUNT_FILE") != null ? 
        System.getenv("GOOGLE_SERVICE_ACCOUNT_FILE") : "friendly-paw-calendar.json";
    private static final String CALENDAR_ID = System.getenv("GOOGLE_CALENDAR_ID") != null ? 
        System.getenv("GOOGLE_CALENDAR_ID") : "primary";

    /**
     * Cria credenciais autorizadas usando Service Account.
     * 
     * @param HTTP_TRANSPORT Transporte HTTP da rede
     * @return Objeto GoogleCredential autorizado
     * @throws IOException Se o arquivo de service account não for encontrado
     */
    private static GoogleCredential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        
        GoogleCredential credential = GoogleCredential.fromStream(
            new FileInputStream(SERVICE_ACCOUNT_FILE_PATH), HTTP_TRANSPORT, JSON_FACTORY)
            .createScoped(SCOPES);
            
        return credential;
    }

    /**
     * Cria uma instância do serviço Calendar.
     * 
     * @return Instância do serviço Calendar
     * @throws IOException Se ocorrer erro de I/O
     * @throws GeneralSecurityException Se ocorrer erro de segurança
     */
    public static Calendar getCalendarService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = getCredentials(HTTP_TRANSPORT);
        
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }

    /**
     * Cria um evento de aplicação de medicamento no Google Calendar.
     * 
     * @param animalName Nome do animal
     * @param medicineName Nome do medicamento
     * @param quantity Quantidade aplicada
     * @param startDateTime Data e hora de início da aplicação
     * @param frequency Frequência de aplicação
     * @param endDateTime Data de fim do tratamento (opcional)
     * @return ID do evento criado
     * @throws IOException Se ocorrer erro de I/O
     * @throws GeneralSecurityException Se ocorrer erro de segurança
     */
    public static String createMedicineApplicationEvent(
            String animalName,
            String medicineName,
            String quantity,
            ZonedDateTime startDateTime,
            MedicineApplication.Frequency frequency,
            ZonedDateTime endDateTime) throws IOException, GeneralSecurityException {
        
        Calendar service = getCalendarService();
        
        Event event = new Event()
                .setSummary("Aplicação de Medicamento - " + animalName)
                .setDescription(String.format(
                    "Medicamento: %s\n" +
                    "Quantidade: %s\n" +
                    "Animal: %s\n" +
                    "Frequência: %s",
                    medicineName, quantity, animalName, frequency
                ));

        // Definir horário de início (início do dia)
        ZonedDateTime startOfDay = startDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        EventDateTime start = new EventDateTime()
                .setDate(new com.google.api.client.util.DateTime(startOfDay.toLocalDate().toString()))
                .setTimeZone(startDateTime.getZone().getId());
        event.setStart(start);

        // Definir horário de fim (fim do dia)
        ZonedDateTime endOfDay = startDateTime.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        EventDateTime end = new EventDateTime()
                .setDate(new com.google.api.client.util.DateTime(endOfDay.toLocalDate().toString()))
                .setTimeZone(startDateTime.getZone().getId());
        event.setEnd(end);

        // Definir regras de recorrência
        if (frequency != null && frequency.isRecurring()) {
            String recurrenceRule = frequency.toRRULE(endDateTime);
            if (recurrenceRule != null) {
                event.setRecurrence(Arrays.asList(recurrenceRule));
            }
        }

        // Inserir o evento
       String calendarId = System.getenv("GOOGLE_CALENDAR_ID");
       event = service.events().insert(calendarId, event).execute();
        
        return event.getId();
    }


    /**
     * Deleta um evento do Google Calendar.
     * 
     * @param eventId ID do evento a ser deletado
     * @throws IOException Se ocorrer erro de I/O
     * @throws GeneralSecurityException Se ocorrer erro de segurança
     */
    public static void deleteEvent(String eventId) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService();
        service.events().delete(CALENDAR_ID, eventId).execute();
    }

    /**
     * Atualiza um evento existente no Google Calendar.
     * 
     * @param eventId ID do evento a ser atualizado
     * @param animalName Nome atualizado do animal
     * @param medicineName Nome atualizado do medicamento
     * @param quantity Quantidade atualizada
     * @param startDateTime Data e hora de início atualizadas
     * @param frequency Frequência atualizada
     * @param endDateTime Data de fim atualizada
     * @throws IOException Se ocorrer erro de I/O
     * @throws GeneralSecurityException Se ocorrer erro de segurança
     */
    public static void updateEvent(
            String eventId,
            String animalName,
            String medicineName,
            String quantity,
            ZonedDateTime startDateTime,
            MedicineApplication.Frequency frequency,
            ZonedDateTime endDateTime) throws IOException, GeneralSecurityException {
        
        Calendar service = getCalendarService();
        
        // Recuperar o evento existente
        Event event = service.events().get(CALENDAR_ID, eventId).execute();
        
        // Atualizar detalhes do evento
        event.setSummary("Aplicação de Medicamento - " + animalName)
             .setDescription(String.format(
                "Medicamento: %s\n" +
                "Quantidade: %s\n" +
                "Animal: %s\n" +
                "Frequência: %s",
                medicineName, quantity, animalName, frequency
             ));

        // Atualizar horário de início
        EventDateTime start = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(startDateTime.toInstant().toEpochMilli()))
                .setTimeZone(startDateTime.getZone().getId());
        event.setStart(start);

        // Atualizar horário de fim
        ZonedDateTime endTime = startDateTime.plusHours(1);
        EventDateTime end = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(endTime.toInstant().toEpochMilli()))
                .setTimeZone(endTime.getZone().getId());
        event.setEnd(end);

        // Atualizar regras de recorrência
        if (frequency != null && frequency.isRecurring()) {
            String recurrenceRule = frequency.toRRULE(endDateTime);
            if (recurrenceRule != null) {
                event.setRecurrence(Arrays.asList(recurrenceRule));
            }
        }

        // Atualizar o evento
        service.events().update(CALENDAR_ID, eventId, event).execute();
    }
}
