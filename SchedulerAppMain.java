package meetingscheduler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


class User {
    private int userId;
    private String name;
    private String email;
    private String role;

    public User(int userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }

    public void display() {
        System.out.println(name + " (" + role + ") - " + email);
    }

    public boolean hasEmail(String value) {
        return email.equalsIgnoreCase(value);
    }
}


class Room {
    private int roomId;
    private String name;
    private int capacity;
    private String equipment;
    private boolean available;

    public Room(int roomId, String name, int capacity, String equipment) {
        this.roomId = roomId;
        this.name = name;
        this.capacity = capacity;
        this.equipment = equipment;
        this.available = true;
    }

    public int getRoomId() { return roomId; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public String getEquipment() { return equipment; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public boolean conflictPolicy() {
        return available;
    }

    public void display() {
        System.out.println(name + " | Capacity: " + capacity
                + " | Equipment: " + equipment);
    }

    public boolean hasCapacity(int attendeeCount) {
        return attendeeCount <= capacity;
    }
}


class TrainingRoom extends Room {
    private String trainerName;
    private double hourlyRate;
    private String trainingType;
    private boolean projectorIncluded;

    public TrainingRoom(int id, String name, int capacity, String equipment,
                        String trainerName, double hourlyRate,
                        String trainingType, boolean projectorIncluded) {
        super(id, name, capacity, equipment);
        this.trainerName = trainerName;
        this.hourlyRate = hourlyRate;
        this.trainingType = trainingType;
        this.projectorIncluded = projectorIncluded;
    }

    public String getTrainerName() { return trainerName; }
    public double getHourlyRate() { return hourlyRate; }
    public String getTrainingType() { return trainingType; }
    public boolean hasProjector() { return projectorIncluded; }

    @Override
    public boolean conflictPolicy() {
        return isAvailable() && projectorIncluded;
    }

    @Override
    public void display() {
        System.out.println("Training Room: " + getName()
                + " | Trainer: " + trainerName
                + " | Type: " + trainingType
                + " | Rate/hour: Rs." + hourlyRate);
    }

    public double calculateCost(int hours) {
        return hourlyRate * hours;
    }
}

// Inheritance: ConferenceRoom extends Room
class ConferenceRoom extends Room {
    private String conferenceType;
    private double hourlyRate;
    private boolean videoConference;
    private String supportStaff;

    public ConferenceRoom(int id, String name, int capacity, String equipment,
                          String conferenceType, double hourlyRate,
                          boolean videoConference, String supportStaff) {
        super(id, name, capacity, equipment);
        this.conferenceType = conferenceType;
        this.hourlyRate = hourlyRate;
        this.videoConference = videoConference;
        this.supportStaff = supportStaff;
    }

    public String getConferenceType() { return conferenceType; }
    public double getHourlyRate() { return hourlyRate; }
    public boolean hasVideoConference() { return videoConference; }
    public String getSupportStaff() { return supportStaff; }

    @Override
    public boolean conflictPolicy() {
        return isAvailable() && videoConference;
    }

    @Override
    public void display() {
        System.out.println("Conference Room: " + getName()
                + " | Type: " + conferenceType
                + " | Video Conference: " + videoConference
                + " | Support: " + supportStaff);
    }

    public double calculateCost(int hours) {
        return hourlyRate * hours;
    }
}


class Meeting {
    private int meetId;
    private String title;
    private User organizer;
    private List<User> attendees;
    private Room room;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;

    public Meeting(int meetId, String title, User organizer,
                   List<User> attendees, Room room,
                   LocalDateTime start, LocalDateTime end) {
        this.meetId = meetId;
        this.title = title;
        this.organizer = organizer;
        this.attendees = new ArrayList<>(attendees);
        this.room = room;
        this.start = start;
        this.end = end;
        this.status = "BOOKED";
    }

    public int getMeetId() { return meetId; }
    public String getTitle() { return title; }
    public User getOrganizer() { return organizer; }
    public List<User> getAttendees() { return new ArrayList<>(attendees); }
    public Room getRoom() { return room; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public String getStatus() { return status; }

    public void setSchedule(Room room, LocalDateTime start, LocalDateTime end) {
        this.room = room;
        this.start = start;
        this.end = end;
    }

    public void setStatus(String status) { this.status = status; }
    public void addAttendee(User user) { attendees.add(user); }

    public boolean conflictsWith(LocalDateTime newStart, LocalDateTime newEnd) {
        return newStart.isBefore(end) && newEnd.isAfter(start);
    }

    public boolean includesUser(User user) {
        return attendees.contains(user);
    }

    public void display() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        System.out.println("Meeting ID: " + meetId + " | Title: " + title
                + " | Room: " + room.getName()
                + " | Start: " + start.format(format)
                + " | End: " + end.format(format)
                + " | Status: " + status);
    }
}


class SchedulerService {
    private List<Room> rooms;
    private List<User> users;
    private List<Meeting> meetings;
    private int nextMeetingId;

    public SchedulerService() {
        rooms = new ArrayList<>();
        users = new ArrayList<>();
        meetings = new ArrayList<>();
        nextMeetingId = 1;
    }

    public void addRoom(Room room) { rooms.add(room); }
    public void addUser(User user) { users.add(user); }

    public boolean isRoomAvailable(Room room, LocalDateTime start,
                                   LocalDateTime end, int attendeeCount,
                                   int ignoredMeetingId) {
        if (room == null || start == null || end == null
                || !start.isBefore(end)
                || !room.hasCapacity(attendeeCount)
                || !room.conflictPolicy()) {
            return false;
        }

        for (Meeting meeting : meetings) {
            if (meeting.getStatus().equals("BOOKED")
                    && meeting.getMeetId() != ignoredMeetingId
                    && meeting.getRoom().getRoomId() == room.getRoomId()
                    && meeting.conflictsWith(start, end)) {
                return false;
            }
        }
        return true;
    }

    
    public Meeting book(Room room, LocalDateTime start, LocalDateTime end,
                        User organizer, List<User> attendees, String title) {
        if (!users.contains(organizer)) {
            System.out.println("Booking failed: Organizer is not registered.");
            return null;
        }

        List<User> invited = new ArrayList<>(attendees);
        if (!invited.contains(organizer)) {
            invited.add(organizer);
        }

        for (User attendee : invited) {
            if (!users.contains(attendee)) {
                System.out.println("Booking failed: Attendee is not registered.");
                return null;
            }
        }

        if (!isRoomAvailable(room, start, end, invited.size(), -1)) {
            System.out.println("Booking failed: Room unavailable or conflict detected.");
            return null;
        }

        Meeting meeting = new Meeting(nextMeetingId++, title, organizer,
                invited, room, start, end);
        meetings.add(meeting);
        System.out.println("Meeting booked successfully!");
        meeting.display();
        return meeting;
    }

   
    public Meeting book(List<User> attendees, Room room, LocalDateTime start,
                        int durationMinutes, User organizer, String title) {
        if (durationMinutes <= 0) {
            System.out.println("Booking failed: Duration must be positive.");
            return null;
        }
        return book(room, start, start.plusMinutes(durationMinutes),
                organizer, attendees, title);
    }

    public boolean reschedule(int meetingId, Room newRoom,
                              LocalDateTime newStart, LocalDateTime newEnd) {
        for (Meeting meeting : meetings) {
            if (meeting.getMeetId() == meetingId
                    && meeting.getStatus().equals("BOOKED")) {
                if (!isRoomAvailable(newRoom, newStart, newEnd,
                        meeting.getAttendees().size(), meetingId)) {
                    System.out.println("Reschedule failed: Conflict detected.");
                    return false;
                }
                meeting.setSchedule(newRoom, newStart, newEnd);
                System.out.println("Meeting rescheduled successfully!");
                meeting.display();
                return true;
            }
        }
        System.out.println("Active meeting not found.");
        return false;
    }

    public void cancel(int meetingId) {
        for (Meeting meeting : meetings) {
            if (meeting.getMeetId() == meetingId
                    && meeting.getStatus().equals("BOOKED")) {
                meeting.setStatus("CANCELLED");
                System.out.println("Meeting cancelled: " + meeting.getTitle());
                return;
            }
        }
        System.out.println("Active meeting not found.");
    }

    public void listByUser(User user) {
        System.out.println("\nMeetings for " + user.getName() + ":");
        for (Meeting meeting : meetings) {
            if (meeting.getStatus().equals("BOOKED")
                    && meeting.includesUser(user)) {
                meeting.display();
            }
        }
    }

    public void printDailyUtilization() {
        System.out.println("\nRoom Utilization:");
        for (Room room : rooms) {
            int count = 0;
            for (Meeting meeting : meetings) {
                if (meeting.getStatus().equals("BOOKED")
                        && meeting.getRoom().getRoomId() == room.getRoomId()) {
                    count++;
                }
            }
            System.out.println(room.getName() + " : " + count + " active meeting(s)");
        }
    }

    public void displayRooms() {
        System.out.println("\nRoom Details:");
        
        for (Room room : rooms) {
            room.display();
        }
    }

    public void displayAllMeetings() {
        System.out.println("\nAll Meetings:");
        for (Meeting meeting : meetings) {
            meeting.display();
        }
    }
}


public class SchedulerAppMain {
    public static void main(String[] args) {
        SchedulerService service = new SchedulerService();

        User user1 = new User(1, "Arun", "arun@gmail.com", "Organizer");
        User user2 = new User(2, "Priya", "priya@gmail.com", "Attendee");
        User user3 = new User(3, "Kumar", "kumar@gmail.com", "Attendee");

        service.addUser(user1);
        service.addUser(user2);
        service.addUser(user3);

        Room room1 = new TrainingRoom(101, "Training Room A", 10,
                "Projector, Whiteboard", "Ravi", 500,
                "Java Training", true);
        Room room2 = new ConferenceRoom(102, "Conference Room B", 20,
                "Screen, Wi-Fi", "Business", 1000, true, "Staff 1");

        service.addRoom(room1);
        service.addRoom(room2);
        service.displayRooms();

        LocalDateTime start = LocalDateTime.of(2026, 10, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 20, 11, 0);

        List<User> attendees = new ArrayList<>();
        attendees.add(user2);
        attendees.add(user3);

        System.out.println("\n--- BOOKING MEETING ---");
        Meeting meeting = service.book(room1, start, end, user1,
                attendees, "Java Project Meeting");

        System.out.println("\n--- TESTING ROOM CONFLICT ---");
        service.book(room1, start.plusMinutes(30), end.plusMinutes(30),
                user1, attendees, "Conflicting Meeting");

        System.out.println("\n--- RESCHEDULING MEETING ---");
        if (meeting != null) {
            service.reschedule(meeting.getMeetId(), room2,
                    start.plusHours(2), end.plusHours(2));
        }

        service.listByUser(user2);
        service.printDailyUtilization();

        System.out.println("\n--- CANCELLING MEETING ---");
        if (meeting != null) {
            service.cancel(meeting.getMeetId());
        }
    }
}