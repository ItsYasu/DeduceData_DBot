package bot;

import bot.database.DatabaseConnection;
import bot.database.DatabaseFunctions;
import bot.listeners.Bot;
import bot.listeners.CommandsHandler;
import bot.reminders.Reminder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.List;
//Test
public class Main {
    private static JDA bot; // Declare JDA instance as a static instance variable
    public static JDA getJDAInstance() {  // Static getter method to retrieve the JDA instance
        return bot;
    }
    public static void main(String args[]) throws InterruptedException {
        //Testing DB connection
        DatabaseConnection dbConnection = new DatabaseConnection();
        DatabaseFunctions dbFunctions = new DatabaseFunctions();
        List<Reminder> reminders = dbFunctions.getUpcomingReminders();
        // Print the results
        /*for (Reminder reminder : reminders) {
            System.out.println("Reminder ID: " + reminder.getReminderId());
            System.out.println("User ID: " + reminder.getUserId());
            System.out.println("Title: " + reminder.getTitle());
            System.out.println("Description: " + reminder.getDescription());
            System.out.println("URL: " + reminder.getUrl());
            System.out.println("Creator: " + reminder.getCreator());
            System.out.println("Date: " + reminder.getDate());
            System.out.println("Message ID: " + reminder.getMessageId());
            System.out.println("-------------------------------");
        }

         */
        try {
            // Creating the bot (default config)x
            bot = JDABuilder.createDefault(Config.getDiscordApiToken())
                    .setActivity(Activity.watching("DeduceData Solutions"))
                    .setStatus(OnlineStatus.ONLINE)
                    .enableCache(CacheFlag.VOICE_STATE)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_WEBHOOKS, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new CommandsHandler())
                    .build().awaitReady();

            // Create an instance of the Bot class
            Bot botInstance = new Bot();
            botInstance.setJDA(bot);  // Set the JDA instance

            // Add event listeners
            bot.addEventListener(botInstance);



        } catch (Exception e) {
            System.out.println("Wrong discord token");
        }

    }
}