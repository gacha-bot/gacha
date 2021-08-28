package xyz.oopsjpeg.gacha;

import discord4j.core.GatewayDiscordClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Manager
{
    Core getCore();

    default GatewayDiscordClient getGateway()
    {
        return getCore().getGateway();
    }

    default Settings getSettings()
    {
        return getCore().getSettings();
    }

    default Logger getLogger()
    {
        return LoggerFactory.getLogger(this.getClass());
    }
}
