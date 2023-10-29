package bot.listeners;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class CommandsHandler extends ListenerAdapter {
    @Override
    public void onGuildReady(GuildReadyEvent event) {
        System.out.println("OnGuildReady invoked for guild: " + event.getGuild().getName());
        //Storing commands in a List
        List<CommandData> commandList = new ArrayList<>();
        //Creating options for register in database:
        OptionData email = new OptionData(OptionType.STRING, "email" , "Type your email to get notifications of your events through mail", true);
        OptionData updatedEmail = new OptionData(OptionType.STRING, "updatedemail","Type the email you want to set up.",true);
        //Adding commands to the list
        commandList.add(Commands.slash("addmeeting","Add a meeting that will be reminded to every participant"));
        commandList.add(Commands.slash("register","Register your discord user on the database to receive notifications through email").addOptions(email));
        commandList.add(Commands.slash("updateemail", "Add or Update your current email to get notified.").addOptions(updatedEmail));
        commandList.add(Commands.slash("profile", "Prints your profile details"));
        commandList.add(Commands.slash("weeklyreminders" , "Prints all the reminders you have for the next week"));
        commandList.add(Commands.slash("enable_vt_reminders","Set to false by default. Just affect to online lessons"));
        //Registering commandList to JDA API (bot)
        event.getGuild().updateCommands().addCommands(commandList).queue();

    }
}
