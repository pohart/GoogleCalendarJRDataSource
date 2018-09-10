package pohart;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.time.format.*;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

 
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRField;

/**
 *  Access a Google Calendar as a Jasper Reports DataSource
 */
public class GoogleCalendarJRDataSource implements JRDataSource{
	
	private final ZonedDateTime start;
	private final ZonedDateTime end;
    /** Application name. */
    private static final String APPLICATION_NAME =
        "Google Calendar JRDataSource";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"),".credentials/calendar-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart
     */ 
    private static final List<String> SCOPES =
        Arrays.asList(CalendarScopes.CALENDAR_READONLY);

    private static final ZoneId TIME_ZONE = ZoneId.systemDefault();
    static {
        try {
            HTTP_TRANSPORT = newProxyTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException if there is an error getting the credential
     */
    public Credential authorize() throws IOException {
        // Load client secrets.
        
        

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * @throws IOException
     */
    private com.google.api.services.calendar.Calendar
        getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static HttpTransport newProxyTransport() throws GeneralSecurityException, IOException, URISyntaxException {
        NetHttpTransport.Builder builder = new NetHttpTransport.Builder();
        builder.trustCertificates(GoogleUtils.getCertificateTrustStore());
        ProxySelector.getDefault().select(new URI(com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants.AUTHORIZATION_SERVER_URL)).forEach(System.out::println);
        builder.setProxy(ProxySelector.getDefault().select(new URI(com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants.AUTHORIZATION_SERVER_URL)).get(0));
        //builder.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 3128)));
        return builder.build();
    }
  

    public static void main(String[] args) throws IOException {
        // Build a new authorized API client service.
        // Note: Do not confuse this class with the
        //   com.google.api.services.calendar.model.Calendar class.
        /*com.google.api.services.calendar.Calendar service = getCalendarService();

        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
            .setMaxResults(10)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute();*/
        List<Event> items = getJRDataSource().items; //events.getItems();
	new Exception().printStackTrace();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            items.forEach((event) -> {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)\n", event.getSummary(), start);
            });
        }
    }
  int index = 0;
  List<Event> items = new ArrayList<>();
  private GoogleClientSecrets clientSecrets;
  public GoogleCalendarJRDataSource(String calendarName, ZonedDateTime startDate, ZonedDateTime endDate, InputStream clientSecretStream) {
        try {
        // Build a new authorized API client service.
        // Note: Do not confuse this class with the
        //   com.google.api.services.calendar.model.Calendar class.
	this.start = startDate;
	this.end = endDate;
        this.clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(clientSecretStream));
        com.google.api.services.calendar.Calendar service = getCalendarService();

        Events oldEvent = service.events().list(calendarName)
          .setTimeMin(new DateTime(startDate.toInstant().toEpochMilli()))
            .setTimeMax(new DateTime(endDate.toInstant().toEpochMilli()))
            .setOrderBy("startTime")
	    .setMaxResults(100)
            .setSingleEvents(true)
            .execute();
        Events events = service.events().list(calendarName)
            .setTimeMin(new DateTime(startDate.toInstant().toEpochMilli()))
            .setTimeMax(new DateTime(endDate.toInstant().toEpochMilli()))
            .setOrderBy("startTime")
			.setMaxResults(100)
            .setSingleEvents(true)
            .execute();
        
        items = events.getItems();
		System.out.println(items.size());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
  }
  public ZonedDateTime getStartTime()
  {
	  return start;
  }
  public ZonedDateTime getEndTime()
  {
	  return end;
  }
  
  private Instant getInstant(EventDateTime edt)
  {
	  DateTime e = edt.getDateTime();
	  if (e == null)
	  {
		  e = edt.getDate();
	  }
	  return Instant.ofEpochMilli(e.getValue());
  }
  
  public Object getFieldValue(JRField field)
  {
	
    String fieldName = field.getName();
    if (fieldName.equalsIgnoreCase("Title"))
    {
      return items.get(index).getSummary();
    }
    
    if (fieldName.equalsIgnoreCase("Creator"))
    {
      return items.get(index).getCreator();
    }
    
    if (fieldName.equalsIgnoreCase("Description"))
    {
      return items.get(index).getDescription();
    }
    
    if (fieldName.equalsIgnoreCase("Start"))
    {
        return LocalDateTime.from(getInstant(items.get(index).getStart()).atZone(TIME_ZONE));
    }
    
    if (fieldName.equalsIgnoreCase("End"))
    {
	  return LocalDateTime.from(getInstant(items.get(index).getEnd()).atZone(TIME_ZONE));
    }
    
    if (fieldName.equalsIgnoreCase("Location"))
    {
      return items.get(index).getLocation();
    }
	if (fieldName.equalsIgnoreCase("Day of week"))
	{
		Instant instant = getInstant(items.get(index).getStart());
		return LocalDate.from(instant.atZone(TIME_ZONE)).getDayOfWeek().getDisplayName(TextStyle.SHORT, java.util.Locale.US);
	}
throw new RuntimeException("Unknown field: " + fieldName);
  }
  public boolean next()
  {
    index++;
    return index < items.size();
  }
  
  public static GoogleCalendarJRDataSource getJRDataSource()
  {
	  return new GoogleCalendarJRDataSource("",//calendar id from your google calendar settings
				LocalDateTime.of(2017,2,13,0,0).atZone(TIME_ZONE), LocalDateTime.of(2017,9,1,0,0).atZone(TIME_ZONE),
                                GoogleCalendarJRDataSource.class.getResourceAsStream("/client_secret.json"));
  }
}
