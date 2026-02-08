package io.lolyay.addon.dupedb.dao;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

public record MeDAO(
    User user
) {
    public record User(
        Integer id,
        String username,
        String role,
        @SerializedName("discord_id") String discordId,
        @SerializedName("discord_username") String discordUsername,
        @SerializedName("discord_avatar") String discordAvatar,
        @SerializedName("display_name") @Nullable Object displayName,
        @SerializedName("custom_avatar") @Nullable Object customAvatar,
        @SerializedName("hide_discord_profile") Integer hideDiscordProfile
    ) {
    }
}
