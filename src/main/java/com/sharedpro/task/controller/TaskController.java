package com.sharedpro.task.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventAttendee;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.TimeZone;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class TaskController 
{

private static final String APPLICATION_NAME = "SharedPro Task";
private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
private static final String TOKENS_DIRECTORY_PATH = "tokens";
private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

@GetMapping("/")
public String process() 
{
return "index";
}

@PostMapping("/createEvent")
public String createMessage( @RequestParam("emails") String emails , @RequestParam("startDate") String startDate , @RequestParam("endDate") String endDate , @RequestParam("summary") String summary , @RequestParam("location") String location , @RequestParam("description") String description) 
{
try
{
String [] emailArray = emails.split("[,]",0);

System.out.println(emails);
System.out.println(startDate);
System.out.println(endDate);
System.out.println(location);
System.out.println(summary);
System.out.println(description);

final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
Event event = new Event().setSummary(summary).setLocation(location).setDescription(description);

DateTime startDateTime = new DateTime(startDate+":00+05:30");
EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone("GMT");
event.setStart(start);

DateTime endDateTime = new DateTime(endDate+":00+05:30");
EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone("GMT");
event.setEnd(end);

EventAttendee[] attendees = new EventAttendee[emailArray.length]; 
for(int i = 0 ; i < emailArray.length ; i++)
{
attendees[i] = new EventAttendee().setEmail(emailArray[i]);
}
    
event.setAttendees(Arrays.asList(attendees));

String calendarId = "primary";
event = service.events().insert(calendarId, event).setSendNotifications(true).execute();

}
catch(Exception e)
{
e.printStackTrace();
}

return "redirect:/";
}

	

private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException 
{
InputStream in = TaskController.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
if (in == null) 
{
throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
}

GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
.setAccessType("offline")
.build();

LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
}

}