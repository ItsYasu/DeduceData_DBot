package bot.listeners;

import bot.database.DatabaseConnection;
import bot.database.DatabaseFunctions;
import bot.reminders.Reminder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.List;


public class Functions {

    public EmbedBuilder getReminderEmbedForMessage(int reminderId) {
        // Connect to the database and retrieve the reminder by its ID
        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection conn = dbConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM REMINDERS WHERE reminder_id = ?");
            ps.setInt(1, reminderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Extract data from the ResultSet
                String title = rs.getString("titulo");
                String description = rs.getString("descripcion");
                String url = rs.getString("url");
                String creator = rs.getString("creador");
                Timestamp timestamp = rs.getTimestamp("fecha");

                // Adjusting description format
                StringBuilder formattedDescription = new StringBuilder(description);

                // Add URL to the description if it's not null
                if (url != null && !url.trim().isEmpty()) {
                    formattedDescription.append("\n\n")
                            .append("**URL:** ")
                            .append(url);
                }

                // Create an EmbedBuilder and populate it with the data
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(title)
                        .setDescription(formattedDescription.toString())
                        .setColor(Color.BLUE)
                        .setFooter("Reminder created by: " + creator + ". Date: " + timestamp);

                // Set URL only if it's not null
                if (url != null && !url.trim().isEmpty()) {
                    embedBuilder.setUrl(url);
                }

                return embedBuilder;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public boolean hasSent24hReminderDm(int reminderId) {
        String query = "SELECT sent_24h_reminder_dm FROM REMINDERS WHERE reminder_id = ?";
        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection connection = dbConnection.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, reminderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean("sent_24h_reminder_dm");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasSent1hReminderDm(int reminderId) {
        String query = "SELECT sent_1h_reminder_dm FROM REMINDERS WHERE reminder_id = ?";
        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection connection = dbConnection.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, reminderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean("sent_1h_reminder_dm");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean hasSent24hReminderEmbed(int reminderId) {
        String query = "SELECT sent_24h_reminder_embed FROM REMINDERS WHERE reminder_id = ?";
        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection connection = dbConnection.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, reminderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean("sent_24h_reminder_embed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean hasSent1hReminderEmbed(int reminderId) {
        String query = "SELECT sent_1h_reminder_embed FROM REMINDERS WHERE reminder_id = ?";
        DatabaseConnection dbConnection = new DatabaseConnection();
        try (Connection connection = dbConnection.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, reminderId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean("sent_1h_reminder_embed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
