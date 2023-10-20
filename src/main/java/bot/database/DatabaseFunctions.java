package bot.database;

import bot.reminders.Reminder;
import bot.users.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseFunctions {

    public void testConnection() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("Connection is valid and working!");
            } else {
                System.out.println("Connection is not valid.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error testing the connection.");
        }
    }
    public Optional<Long> findUserIdByDiscordId(Long discordId) {
        String query = "SELECT user_id FROM USERS WHERE discord_id = ?";
        Long userId = null;
        try(Connection connection = DatabaseConnection.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setLong(1, discordId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                userId = rs.getLong("user_id");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(userId);

    }
    public Long findDiscordIdByUserId(long userId) {
        Long discordId = null;
        String query = "SELECT discord_id FROM USERS WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); // Assuming you have a DatabaseConnection class
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    discordId = rs.getLong("discord_id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return discordId;
    }
    public Optional<User> getUserDetailsByDiscordId(Long discordId) {
        String query = "SELECT user_id, discord_id, discord_name, email FROM USERS WHERE discord_id = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, discordId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setDiscordId(rs.getLong("discord_id"));
                user.setDiscordName(rs.getString("discord_name"));
                user.setEmail(rs.getString("email"));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean updateEmailForUser(Long discordId, String newEmail) {
        String query = "UPDATE USERS SET email = ? WHERE discord_id = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, newEmail);
            preparedStatement.setLong(2, discordId);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Indicates an error
        }
    }


    public int insertMeeting(long userId, String title, String description, String url, String creator, Timestamp date, Long message_id) {
        String query = "INSERT INTO REMINDERS(user_id, titulo, descripcion, url, creador, fecha, message_id) VALUES(?,?,?,?,?,?,?)";
        try(Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, description);
            preparedStatement.setString(4, url);
            preparedStatement.setString(5, creator);
            preparedStatement.setTimestamp(6, date);
            preparedStatement.setLong(7, message_id);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;  // Indicates an error
    }
    public void insertDiscordId(Long userId, Integer reminderId, Long discordId) {
        String sql = "UPDATE MEETING_ATTENDEES SET discord_ID = ? WHERE user_id = ? AND reminder_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, discordId);
            pstmt.setLong(2, userId);
            pstmt.setInt(3, reminderId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void removeAttendeeAndDiscordIdFromMeeting(Long userId, Integer reminderId) {
        String sql = "DELETE FROM MEETING_ATTENDEES WHERE user_id = ? AND reminder_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setInt(2, reminderId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public int registerUser(long discordId, String discordName, String email) {
        String query = "INSERT INTO USERS(discord_id, discord_name, email) VALUES(?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, discordId);
            preparedStatement.setString(2, discordName);
            preparedStatement.setString(3, email);
            preparedStatement.executeUpdate();
            return 1;  // Successfully registered
        } catch (SQLException e) {
            // Check if the error is due to a unique constraint violation
            if(e.getErrorCode() == 1062) {  // Error code for duplicate entry.
                return 0;  // User already exists
            } else {
                e.printStackTrace();
                return -1; // General error
            }
        }
    }
    public void addAttendeeToMeeting(long userId, int reminderId) {
        String query = "INSERT INTO MEETING_ATTENDEES(user_id, reminder_id) VALUES(?, ?)";

        try(Connection connection = DatabaseConnection.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(query);
        ) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setInt(2, reminderId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public List<Long> getAttendeesForMeeting(int reminderId) {
        List<Long> attendees = new ArrayList<>();
        String query = "SELECT user_id FROM MEETING_ATTENDEES WHERE reminder_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, reminderId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                attendees.add(rs.getLong("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendees;
    }
    public int getReminderIdByMessageId(String messageId) {
        String query = "SELECT reminder_id FROM REMINDERS WHERE message_id = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ) {
            preparedStatement.setString(1, messageId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                System.out.println("Fetching reminder for message ID: " + messageId);

                if (resultSet.next()) {
                    return resultSet.getInt("reminder_id");
                } else {
                    return -1;  // Return -1 if no entry found.
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;  // Return -1 on exception.
        }
    }
    public List<Reminder> getUpcomingReminders() {
        List<Reminder> upcomingReminders = new ArrayList<>();
        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection conn = dbConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM REMINDERS WHERE fecha > CURRENT_TIMESTAMP");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int reminderId = rs.getInt("reminder_id");
                long userId = rs.getLong("user_id");
                String title = rs.getString("titulo");
                String description = rs.getString("descripcion");
                String url = rs.getString("url");
                String creator = rs.getString("creador");
                Timestamp timestamp = rs.getTimestamp("fecha");
                long messageId = rs.getLong("message_id");

                Reminder reminder = new Reminder(reminderId, userId, title, description, url, creator, timestamp, messageId); // Assuming you have a Reminder class with this constructor.
                upcomingReminders.add(reminder);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return upcomingReminders;
    }
    public void set24hReminderSentDm(int reminderId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE REMINDERS SET sent_24h_reminder_dm = TRUE WHERE reminder_id = ?")) {
            ps.setInt(1, reminderId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void set1hReminderSentDm(int reminderId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE REMINDERS SET sent_1h_reminder_dm = TRUE WHERE reminder_id = ?")) {
            ps.setInt(1, reminderId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public void set24hReminderSentEmbed(int reminderId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE REMINDERS SET sent_24h_reminder_embed = TRUE WHERE reminder_id = ?")) {
            ps.setInt(1, reminderId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void set1hReminderSentEmbed(int reminderId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE REMINDERS SET sent_1h_reminder_embed = TRUE WHERE reminder_id = ?")) {
            ps.setInt(1, reminderId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public void updateMessageIdForReminder(int reminderId, long messageId) {
        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection conn = dbConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE REMINDERS SET message_id = ? WHERE reminder_id = ?");
            ps.setLong(1, messageId);
            ps.setInt(2, reminderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
