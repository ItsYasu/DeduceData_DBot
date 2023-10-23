package bot.listeners;

import bot.Main;
import bot.database.DatabaseConnection;
import bot.database.DatabaseFunctions;
import bot.reminders.Reminder;
import bot.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.w3c.dom.Text;

import java.awt.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Bot extends ListenerAdapter {
    private final Map<String, Integer> messageIdToReminderIdMap = new HashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private JDA jda;

    DatabaseFunctions dbFunctions = new DatabaseFunctions();
    Functions functions = new Functions();
    private static final long channelID = 1166022507594264626L; // 'L' at the end to mark it as a long literal


    public void setJDA(JDA jda) {
        this.jda = jda;
        initScheduler();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("addmeeting")) {
            event.deferReply().queue();
            Guild guild = event.getGuild();
            if (guild == null) {
                event.getHook().sendMessage("This command can only be used in a Server").setEphemeral(true).queue();
                return;
            }

            //Buttons
            Button meeting = Button.success("meeting", "Set up a meeting");
            Button profilebutton = Button.primary("profilebtn", "Profile page");
            //Message Builder
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("DeduceData Reminder System")
                    .setDescription("Hello " + event.getUser().getAsMention() + ",\n\n" +
                            "Would you like to set up a reminder for a meeting or an event?" + " \n \n " + "By doing so, every user who reacts with the ✔ emoji will receive a direct message as a reminder and will also be mentioned in the server." + "\n" + "Please choose your desired option below.")
                    .setColor(Color.BLUE) // Optional: You can set a color for the embed. This is just an example.
                    .setFooter("DeduceData Reminder Service")
                    .setUrl("https://deducedata.solutions/es/"); // Optional: Adding a footer for additional context or branding.
            event.getHook().sendMessageEmbeds(embedBuilder.build()).setActionRow(meeting, profilebutton).queue();

        } else if (event.getName().equals("register")) {
            event.deferReply().queue();
            String userEmail = event.getOption("email").getAsString();
            int registerResult = dbFunctions.registerUser(event.getUser().getIdLong(), event.getUser().getName(), userEmail);
            String responseMessage;

            switch (registerResult) {
                case 1:
                    responseMessage = "Successfully registered!";
                    break;
                case 0:
                    responseMessage = "User already exists.";
                    break;
                default:
                    responseMessage = "There was an error trying to register the user.";
                    break;
            }
            event.getHook().sendMessage(responseMessage).queue();
        } else if (event.getName().equals("updateemail")) {
            event.deferReply().queue();
            long discordID = event.getUser().getIdLong();
            String newEmail = event.getOption("updatedemail").getAsString();

            if (dbFunctions.updateEmailForUser(discordID, newEmail)) {
                event.getHook().sendMessage("Successfully updated the email to: " + "```" + newEmail + "```").queue();
            } else {
                event.getHook().sendMessage("An error occurred while updating the email. Please try again later.").queue();
            }

        } else if (event.getName().equals("profile")) {
            event.deferReply().queue();
            Optional<bot.users.User> optionalUser = dbFunctions.getUserDetailsByDiscordId(event.getUser().getIdLong());
            Button updateEmail = Button.success("updateEmail", "Update Email");

            optionalUser.ifPresentOrElse(user -> {
                // If the user is registered (i.e., optionalUser contains a User)
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("DeduceData Reminder System")
                        .setDescription("Hello " + event.getUser().getAsMention() + ",\n\n" +
                                "Here you have your profile details: " + " \n \n \n " +
                                "User ID: " + user.getUserId() + "\n \n" +
                                "Discord ID: " + user.getDiscordId() + "\n\n" +
                                "Discord Name: " + user.getDiscordName() + "\n\n" +
                                "Email: " + user.getEmail())
                        .setColor(Color.CYAN)
                        .setFooter("DeduceData Reminder Service")
                        .setUrl("https://deducedata.solutions/es/");
                event.getHook().sendMessageEmbeds(embedBuilder.build()).setActionRow(updateEmail).queue();
            }, () -> {
                // If the user is not registered (i.e., optionalUser is empty)
                event.getHook().sendMessage("**User is not registered.** \n\nUse /register to register yourself.").queue();
            });
        } else if (event.getName().equals("weeklyreminders")) {
            event.deferReply().queue();

            long discordId = event.getUser().getIdLong();
            List<Reminder> weeklyReminders = dbFunctions.getWeeklyReminders(discordId);

            if (weeklyReminders.isEmpty()) {
                event.getHook().sendMessage("**You have no reminders for the next week.**").queue();
            } else {
                for (Reminder reminder : weeklyReminders) {
                    EmbedBuilder embed = functions.getReminderEmbedForMessage(reminder.getReminderId());
                    event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
                }
            }
        }
    }


    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("meeting")) {
            TextInput meetingTitle = TextInput.create("meetingTitle", "Meeting title", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setPlaceholder("Set a title for the meeting")
                    .build();
            TextInput meetingDescription = TextInput.create("meetingDesc", "Description", TextInputStyle.PARAGRAPH)
                    .setRequired(true)
                    .setPlaceholder("Describe the meeting's purpose")
                    .build();
            TextInput meetingDate = TextInput.create("meetingDate", "Date", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setPlaceholder("DD/MM/YYYY HH:mm")
                    .build();
            TextInput projectUrl = TextInput.create("projectUrl", "Project URL", TextInputStyle.SHORT)
                    .setRequired(false)
                    .setPlaceholder("https://github.com/user/repository")
                    .build();
            Modal meetingModal = Modal.create("meetingModal", "Meeting Reminder")
                    .addActionRow(meetingTitle)
                    .addActionRow(meetingDescription)
                    .addActionRow(meetingDate)
                    .addActionRow(projectUrl)
                    .build();
            event.replyModal(meetingModal).queue();
        } else if (event.getComponentId().equals("updateEmail")) {
            TextInput newEmail = TextInput.create("newEmail", "Set up your new email", TextInputStyle.SHORT)
                    .setRequired(true)
                    .setMinLength(1)
                    .setPlaceholder("example@gmail.com")
                    .build();
            Modal newEmailModal = Modal.create("UpdateEmailModal", "Email Change")
                    .addActionRow(newEmail).build();
            event.replyModal(newEmailModal).queue();
        } else if (event.getComponentId().equals("profilebtn")) {
            event.deferReply().queue();
            Optional<bot.users.User> optionalUser = dbFunctions.getUserDetailsByDiscordId(event.getUser().getIdLong());
            Button updateEmail = Button.success("updateEmail", "Update Email");
            optionalUser.ifPresentOrElse(user -> {
                // If the user is registered (i.e., optionalUser contains a User)
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("DeduceData Reminder System")
                        .setDescription("Hello " + event.getUser().getAsMention() + ",\n\n" +
                                "Here you have your profile details: " + " \n \n \n " +
                                "User ID: " + user.getUserId() + "\n \n" +
                                "Discord ID: " + user.getDiscordId() + "\n\n" +
                                "Discord Name: " + user.getDiscordName() + "\n\n" +
                                "Email: " + user.getEmail())
                        .setColor(Color.CYAN)
                        .setFooter("DeduceData Reminder Service")
                        .setUrl("https://deducedata.solutions/es/");
                event.getHook().sendMessageEmbeds(embedBuilder.build()).setActionRow(updateEmail).queue();
            }, () -> {
                // If the user is not registered (i.e., optionalUser is empty)
                event.getHook().sendMessage("**User is not registered.** \n\nUse /register to register yourself.").queue();
            });
        }
    }


    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("meetingModal")) {
            event.deferReply().queue();
            String meetingTitle = event.getValue("meetingTitle").getAsString();
            String meetingDescription = event.getValue("meetingDesc").getAsString();
            String meetingDateString = event.getValue("meetingDate").getAsString();
            String projectUrl = event.getValue("projectUrl").getAsString();
            Date meetingDate;
            Optional<Long> userIdOpt = dbFunctions.findUserIdByDiscordId(event.getUser().getIdLong());
            if (!userIdOpt.isPresent()) {
                event.getHook().sendMessage("**User not found in the database**." + "\n\n" + "**User had been registered** " + "\n" + "You can add meetings now!").queue();
                dbFunctions.registerUser(event.getUser().getIdLong(), event.getUser().getName(), null);
                return;  // Exit early since we don't have a valid user.
            }
            long userId = userIdOpt.get();  // Retrieve the long value

            if (Utils.isValidDateFormat(meetingDateString)) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                try {
                    meetingDate = formatter.parse(meetingDateString);
                    System.out.println(meetingDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                    event.getHook().sendMessage("Failed to parse the date format. Please ensure it's in the format DD/MM/YYYY HH:mm.").queue();
                    return;
                }
            } else {
                event.getHook().sendMessage("The date format you entered is incorrect. Please use the format DD/MM/YYYY HH:mm.").queue();
                return;
            }

            EmbedBuilder embedMessage = new EmbedBuilder();
            embedMessage.setTitle("New Meeting: " + meetingTitle)
                    .setDescription(meetingDescription + "\n\n" + "Meeting Date : " + meetingDateString + "\n\n" + "Project url: " + projectUrl + "\n\n" + "Created by: " + event.getUser().getAsMention())
                    .setColor(Color.green)
                    .setFooter("React to the message to get reminded!")
                    .setUrl(projectUrl);
            Message sentMessage = event.getHook().sendMessageEmbeds(embedMessage.build()).complete();
            long messageId = sentMessage.getIdLong();
            sentMessage.addReaction(Emoji.fromUnicode("✔")).queue();

            //Storing the reminder on the database
            int reminderId = dbFunctions.insertMeeting(userId, meetingTitle, meetingDescription, projectUrl, event.getUser().getName(), new java.sql.Timestamp(meetingDate.getTime()), messageId);
            if (reminderId != -1) {
                messageIdToReminderIdMap.put(sentMessage.getId(), reminderId);
                event.getHook().sendMessage("**Meeting saved with ID:** " + reminderId).queue();
                System.out.println("Meeting saved with ID: " + reminderId);
            } else {
                System.out.println("Failed to save the meeting to the database.");
            }
        } else if (event.getModalId().equals("UpdateEmailModal")) {
            event.deferReply().queue();
            String newEmail = event.getValue("newEmail").getAsString();
            if (dbFunctions.updateEmailForUser(event.getUser().getIdLong(), newEmail)) {
                event.getHook().sendMessage("Successfully updated the email to:" + "```" + newEmail + "```").queue();
            } else {
                event.getHook().sendMessage("An error occurred while updating the email. Please try again later.").queue();
            }

        }
    }

    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            return; // Exit early if the event is triggered by the bot
        }
        String messageID = event.getMessageId();
        Integer reminderId = dbFunctions.getReminderIdByMessageId(messageID);
        long discord_id = event.getUserIdLong();
        Optional<Long> userIdOpt = dbFunctions.findUserIdByDiscordId(event.getUser().getIdLong());
        if (!userIdOpt.isPresent()) {
            System.out.println("User not found: onMessageReactionAdd method.");
            event.getChannel().sendMessage("**User not found in the database**." + "\n\n" + "**User had been registered** " + "\n" + "You can add meetings now!").queue();
            dbFunctions.registerUser(event.getUser().getIdLong(), event.getUser().getName(), null);
            return;  // Exit early since we don't have a valid user.
        }
        long userId = userIdOpt.get();  // Retrieve the long value
        System.out.println("onMessageReactionAdd userId: " + userId);

        if (event.getReaction().getEmoji().equals(Emoji.fromUnicode("✔"))
                && !event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            User user = event.getUser();

            if (reminderId != null && reminderId != -1) {
                dbFunctions.addAttendeeToMeeting(userId,discord_id, reminderId);
                dbFunctions.insertDiscordId(userId, reminderId, event.getUser().getIdLong());

            }
            EmbedBuilder reminderEmbed = functions.getReminderEmbedForMessage(reminderId);

            if (reminderEmbed != null) {
                user.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage("**Successfully subscribed to this meeting: **").addEmbeds(reminderEmbed.build()).queue();
                }, throwable -> {
                    System.out.println("Failed to send DM to " + user.getName() + ": " + throwable.getMessage());
                });
            }
        }
    }


    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            return; // Exit early if the event is triggered by the bot
        }
        Optional<Long> userIdOpt = dbFunctions.findUserIdByDiscordId(event.getUser().getIdLong());
        long userId;
        String messageID = event.getMessageId();
        Integer reminderId = dbFunctions.getReminderIdByMessageId(messageID);
        if (!userIdOpt.isPresent()) {
            System.out.println("No user found for Discord ID: (onmessagereactionremove)" + event.getUser().getIdLong());
            return;  // Exit early since we don't have a valid user.
        }
        userId = userIdOpt.get();
        if (event.getReaction().getEmoji().equals(Emoji.fromUnicode("✔"))
                && !event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            User user = event.getUser();

            if (reminderId != null && reminderId != -1) {
                dbFunctions.removeAttendeeAndDiscordIdFromMeeting(userId, reminderId);
            }
            EmbedBuilder reminderEmbed = functions.getReminderEmbedForMessage(reminderId);

            if (reminderEmbed != null) {
                user.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage("**Unsuscribed: **").addEmbeds(reminderEmbed.build()).queue();
                }, throwable -> {
                    System.out.println("Failed to send DM to " + user.getName() + ": " + throwable.getMessage());
                });
            }
        }
    }

    private void sendReminders() {
        try {
            java.util.Date now = new java.util.Date();
            System.out.println("sendReminder method initialized: " + now);
            List<Reminder> upcomingReminders = dbFunctions.getUpcomingReminders();

            for (Reminder reminder : upcomingReminders) {
                long diff = reminder.getDate().getTime() - now.getTime();
                long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                long hours = diffMinutes / 60;
                long minutes = diffMinutes % 60;
                //System.out.println("Diff minutes: " + diffMinutes);

                boolean is24HrReminder = diffMinutes >= 1400 && diffMinutes <= 1500;
                boolean is1HrReminder = diffMinutes >= 56 && diffMinutes <= 64;

                if (is24HrReminder && !functions.hasSent24hReminderDm(reminder.getReminderId())) {
                    sendReminderToAttendees(reminder, hours, minutes);
                    dbFunctions.set24hReminderSentDm(reminder.getReminderId());
                } else if (is1HrReminder && !functions.hasSent1hReminderDm(reminder.getReminderId())) {
                    sendReminderToAttendees(reminder, hours, minutes);
                    dbFunctions.set1hReminderSentDm(reminder.getReminderId());
                } else if (is24HrReminder && !functions.hasSent24hReminderEmbed(reminder.getReminderId())) {
                    sendReminderToEmbed(reminder, hours, minutes, channelID);
                    dbFunctions.set24hReminderSentEmbed(reminder.getReminderId());
                } else if (is1HrReminder && !functions.hasSent1hReminderEmbed(reminder.getReminderId())) {
                    sendReminderToEmbed(reminder, hours, minutes, channelID);
                    dbFunctions.set1hReminderSentEmbed(reminder.getReminderId());
                    System.out.println("removeme line 337 bot");
                }
            }

        } catch (Exception e) {
            // Log the exception (preferably with a logging framework)
            System.err.println("An error occurred while sending reminders: " + e.getMessage());
        }
    }

    private void sendReminderToEmbed(Reminder reminder, long hours, long minutes, long channelId) {
        EmbedBuilder reminderEmbed = functions.getReminderEmbedForMessage(reminder.getReminderId());
        TextChannel channel = jda.getTextChannelById(channelId);

        if (channel == null || reminderEmbed == null) {
            return;
        }

        // Fetch all attendees for the meeting from the database
        List<Long> attendees = dbFunctions.getAttendeesForMeeting(reminder.getReminderId());

        // Convert the list of attendee IDs into mentions
        List<String> mentions = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(attendees.size()); // used for synchronization
        for (Long userId : attendees) {
            Long discordID = dbFunctions.findDiscordIdByUserId(userId);
            jda.retrieveUserById(discordID).queue(user -> {
                if (user != null) {
                    mentions.add(user.getAsMention());
                }
                latch.countDown(); // decrease the count
            }, throwable -> {
                System.err.println("Failed to retrieve user with ID: " + discordID);
                latch.countDown(); // decrease the count even on failure
            });
        }
        try {
            latch.await(); // wait for all users to be fetched
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for user fetch: " + e.getMessage());
        }

        String mentionsString = String.join(" ", mentions);
        System.out.println("sendReminderToEmbed: " + mentionsString);
        String reminderMessage = mentionsString + " **Reminder:** There is a meeting in approximately " + hours + " hours and " + minutes + " minutes.";

        channel.sendMessage(reminderMessage).addEmbeds(reminderEmbed.build()).queue(success -> {
            long messageId = success.getIdLong();
            dbFunctions.updateMessageIdForReminder(reminder.getReminderId(), messageId);
        });
    }


    private void sendReminderToAttendees(Reminder reminder, long hours, long minutes) {
        List<Long> dbAttendees = dbFunctions.getAttendeesForMeeting(reminder.getReminderId());
        EmbedBuilder reminderEmbed = functions.getReminderEmbedForMessage(reminder.getReminderId());
        System.out.println("sendReminderToAttendees initialize: ");
        if (reminderEmbed == null) {
            // Generate the embed if it's null.
            reminderEmbed = generateEmbedForReminder(reminder);
        }

        if (reminderEmbed == null) {
            // If still null, log an error and return.
            System.err.println("Failed to generate embed for reminder ID: " + reminder.getReminderId());
            return;
        }

        final EmbedBuilder finalEmbed = reminderEmbed; // Final reference for the lambda

        for (Long userId : dbAttendees) {
            Long discordID = dbFunctions.findDiscordIdByUserId(userId);
            jda.retrieveUserById(discordID).queue(user -> {
                // This callback is executed once the user is successfully retrieved.
                if (user != null) {  // Ensure user is not null before proceeding
                    user.openPrivateChannel().queue(privateChannel -> {
                        System.out.println("Successfully opened private channel for user: " + user.getName() + " (" + user.getId() + ")");
                        privateChannel.sendMessage("**Reminder:** You have a meeting in approximately " + hours + " hours and " + minutes + " minutes.")
                                .addEmbeds(finalEmbed.build()).queue(null, throwable -> {
                                    System.err.println("Failed to send DM to user: " + user.getName() + " (" + user.getId() + ")");
                                    throwable.printStackTrace();
                                });
                    }, throwable -> {
                        System.err.println("Failed to open private channel for user: " + user.getName() + " (" + discordID + ")");
                        throwable.printStackTrace();
                    });
                }
            }, throwable -> {
                System.err.println("Failed to retrieve user with ID: " + discordID);
                throwable.printStackTrace();
            });
        }
    }

    private EmbedBuilder generateEmbedForReminder(Reminder reminder) {
        // Extract data from the Reminder object
        String title = reminder.getTitle();
        String description = reminder.getDescription();
        String url = reminder.getUrl();
        String creator = reminder.getCreator();
        Timestamp timestamp = reminder.getDate();

        // Create an EmbedBuilder and populate it with the data
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title)
                .setDescription(description + "\n\n" + "URL: " + url)
                .setUrl(url)
                .setColor(Color.BLUE)
                .setFooter("Reminder created by: " + creator + ". Date: " + timestamp);

        return embedBuilder;
    }


    public void initScheduler() {
        scheduler.scheduleAtFixedRate(this::sendReminders, 0, 30, TimeUnit.SECONDS);
    }

}

