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

    private static final String APPLICATION_NAME =
        System.getenv("GOOGLE_CALENDAR_APPLICATION_NAME") != null ?
            System.getenv("GOOGLE_CALENDAR_APPLICATION_NAME") : "Pata Amiga";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    private static final String SERVICE_ACCOUNT_FILE_PATH =
        System.getenv("GOOGLE_SERVICE_ACCOUNT_FILE") != null ?
            System.getenv("GOOGLE_SERVICE_ACCOUNT_FILE") : "friendly-paw-calendar.json";

    private static final String CALENDAR_ID =
        System.getenv("GOOGLE_CALENDAR_ID") != null ?
            System.getenv("GOOGLE_CALENDAR_ID") : "primary";

    private static GoogleCredential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        return GoogleCredential.fromStream(
                new FileInputStream(SERVICE_ACCOUNT_FILE_PATH), HTTP_TRANSPORT, JSON_FACTORY
        ).createScoped(SCOPES);
    }

    public static Calendar getCalendarService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = getCredentials(HTTP_TRANSPORT);
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /** cria evento de DIA INTEIRO (consistente) */
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
                        "Medicamento: %s%nQuantidade: %s%nAnimal: %s%nFrequência: %s",
                        medicineName, quantity, animalName,
                        frequency != null ? frequency.getDisplayName() : "Não se repete"
                ));

        // Evento de dia inteiro
        String dateStr = startDateTime.toLocalDate().toString(); // yyyy-MM-dd
        EventDateTime allDay = new EventDateTime()
                .setDate(new com.google.api.client.util.DateTime(dateStr))
                .setTimeZone(startDateTime.getZone().getId());
        event.setStart(allDay);
        event.setEnd(allDay);

        // Recorrência
        if (frequency != null && frequency.isRecurring()) {
            String rrule = frequency.toRRULE(endDateTime);
            if (rrule != null) event.setRecurrence(Arrays.asList(rrule));
        }

        String calendarId = System.getenv("GOOGLE_CALENDAR_ID");
        if (calendarId == null || calendarId.isBlank()) calendarId = CALENDAR_ID;

        event = service.events().insert(calendarId, event).execute();
        return event.getId();
    }

    public static void deleteEvent(String eventId) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService();
        service.events().delete(CALENDAR_ID, eventId).execute();
    }

    /** atualiza mantendo dia inteiro (coerente com create) */
    public static void updateEvent(
            String eventId,
            String animalName,
            String medicineName,
            String quantity,
            ZonedDateTime startDateTime,
            MedicineApplication.Frequency frequency,
            ZonedDateTime endDateTime) throws IOException, GeneralSecurityException {

        Calendar service = getCalendarService();
        Event event = service.events().get(CALENDAR_ID, eventId).execute();

        event.setSummary("Aplicação de Medicamento - " + animalName)
             .setDescription(String.format(
                     "Medicamento: %s%nQuantidade: %s%nAnimal: %s%nFrequência: %s",
                     medicineName, quantity, animalName,
                     frequency != null ? frequency.getDisplayName() : "Não se repete"
             ));

        String dateStr = startDateTime.toLocalDate().toString();
        EventDateTime allDay = new EventDateTime()
                .setDate(new com.google.api.client.util.DateTime(dateStr))
                .setTimeZone(startDateTime.getZone().getId());
        event.setStart(allDay);
        event.setEnd(allDay);

        if (frequency != null && frequency.isRecurring()) {
            String rrule = frequency.toRRULE(endDateTime);
            if (rrule != null) event.setRecurrence(Arrays.asList(rrule));
        } else {
            event.setRecurrence(null);
        }

        service.events().update(CALENDAR_ID, eventId, event).execute();
    }
}
