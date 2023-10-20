package bot.users;

public class User {
    private Long userId;
    private Long discordId;
    private String discordName;
    private String email;

    public User(){

    }

    public User(Long userId, Long discordId, String discordName, String email) {
        this.userId = userId;
        this.discordId = discordId;
        this.discordName = discordName;
        this.email = email;
    }
    public User(Long userId, Long discordId, String discordName){
        this.userId = userId;
        this.discordId = discordId;
        this.discordName = discordName;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getDiscordId() {
        return discordId;
    }

    public String getDiscordName() {
        return discordName;
    }

    public String getEmail() {
        return email;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setDiscordId(Long discordId) {
        this.discordId = discordId;
    }

    public void setDiscordName(String discordName) {
        this.discordName = discordName;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
