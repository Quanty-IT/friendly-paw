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
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarService {

    private static final String APPLICATION_NAME =
        System.getenv("GOOGLE_CALENDAR_APPLICATION_NAME") != null
            ? System.getenv("GOOGLE_CALENDAR_APPLICATION_NAME")
            : "Pata Amiga";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    private static final String SERVICE_ACCOUNT_FILE_PATH =
        System.getenv("GOOGLE_SERVICE_ACCOUNT_FILE") != null
            ? System.getenv("GOOGLE_SERVICE_ACCOUNT_FILE")
            : "friendly-paw-calendar.json";

    // Fallback padrão: "primary"
    private static final String CALENDAR_ID_FALLBACK =
        System.getenv("GOOGLE_CALENDAR_ID") != null
            ? System.getenv("GOOGLE_CALENDAR_ID")
            : "primary";

    /** Calendar ID efetivo (env ou fallback) */
    private static String getCalendarId() {
        String env = System.getenv("GOOGLE_CALENDAR_ID");
        return (env != null && !env.isBlank()) ? env : CALENDAR_ID_FALLBACK;
    }

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

    /** Cria um evento de DIA INTEIRO (end.date é exclusivo → usamos start + 1 dia) */
    public static String createMedicineApplicationEvent(
            String animalName,
            String medicineName,
            String quantity,
            ZonedDateTime startDateTime,
            MedicineApplication.Frequency frequency,
            ZonedDateTime endDateTime
    ) throws IOException, GeneralSecurityException {

        Calendar service = getCalendarService();

        Event event = new Event()
                .setSummary("Aplicação de Medicamento - " + animalName)
                .setDescription(String.format(
                        "Medicamento: %s%nQuantidade: %s%nAnimal: %s%nFrequência: %s",
                        medicineName,
                        quantity,
                        animalName,
                        (frequency != null ? frequency.getDisplayName() : "Não se repete")
                ));

        // All-day: end é EXCLUSIVO → 1 dia após o start
        LocalDate startDate = startDateTime.toLocalDate();
        LocalDate endExclusive = startDate.plusDays(1);

        EventDateTime start = new EventDateTime()
                .setDate(new com.google.api.client.util.DateTime(startDate.toString()));
        EventDateTime end = new EventDateTime()
                .setDate(new com.google.api.client.util.DateTime(endExclusive.toString()));

        event.setStart(start);
        event.setEnd(end);

        // Recorrência (se houver)
        if (frequency != null && frequency.isRecurring()) {
            String rrule = frequency.toRRULE(endDateTime);
            if (rrule != null) event.setRecurrence(Arrays.asList(rrule));
        }

        String calendarId = getCalendarId();
        event = service.events().insert(calendarId, event).execute();
        return event.getId();
    }

    /** Deleta um evento pelo ID */
    public static void deleteEvent(String googleCalendarId) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService();
        service.events().delete(getCalendarId(), googleCalendarId).execute();
    }

    /** Atualiza o evento mantendo DIA INTEIRO e coerência com a criação */
    public static void updateEvent(
            String googleCalendarId,
            String animalName,
            String medicineName,
            String quantity,
            ZonedDateTime startDateTime,
            MedicineApplication.Frequency frequency,
            ZonedDateTime endDateTime
    ) throws IOException, GeneralSecurityException {

        Calendar service = getCalendarService();
        String calendarId = getCalendarId();

        Event event = service.events().get(calendarId, googleCalendarId).execute();

        event.setSummary("Aplicação de Medicamento - " + animalName)
             .setDescription(String.format(
                     "Medicamento: %s%nQuantidade: %s%nAnimal: %s%nFrequência: %s",
                     medicineName,
                     quantity,
                     animalName,
                     (frequency != null ? frequency.getDisplayName() : "Não se repete")
             ));

        // All-day (end exclusivo)
        LocalDate startDate = startDateTime.toLocalDate();
        LocalDate endExclusive = startDate.plusDays(1);

        EventDateTime start = new EventDateTime()
                .setDate(new com.google.api.client.util.DateTime(startDate.toString()));
        EventDateTime end = new EventDateTime()
                .setDate(new com.google.api.client.util.DateTime(endExclusive.toString()));

        event.setStart(start);
        event.setEnd(end);

        if (frequency != null && frequency.isRecurring()) {
            String rrule = frequency.toRRULE(endDateTime);
            event.setRecurrence(rrule != null ? Arrays.asList(rrule) : null);
        } else {
            event.setRecurrence(null);
        }

        service.events().update(calendarId, googleCalendarId, event).execute();
    }
}
