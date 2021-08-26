package xyz.oopsjpeg.gacha;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.oopsjpeg.gacha.command.Replies;
import xyz.oopsjpeg.gacha.object.Vote;
import xyz.oopsjpeg.gacha.object.data.VoteData;
import xyz.oopsjpeg.gacha.object.user.Profile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;

public class GachaServer
{
    private static final Logger logger = LoggerFactory.getLogger(GachaServer.class);

    private final Gacha gacha;
    private final int port;

    private HttpServer server;

    public GachaServer(Gacha gacha, int port)
    {
        this.gacha = gacha;
        this.port = port;
    }

    public final GachaServer start() throws IOException
    {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/vote", new VoteHandler());
        server.setExecutor(null);
        server.start();

        logger.info("Server listening at http://localhost:" + port);

        return this;
    }

    public Gacha getGacha()
    {
        return gacha;
    }

    public int getPort()
    {
        return port;
    }

    class VoteHandler implements HttpHandler
    {
        @Override
        public void handle(HttpExchange t) throws IOException
        {
            try (InputStreamReader isr = new InputStreamReader(t.getRequestBody());)
            {
                VoteData data = Gacha.GSON.fromJson(isr, VoteData.class);
                Vote vote = new Vote(gacha, data);
                User user = vote.getUser();

                Profile profile = gacha.hasProfile(user) ? gacha.getProfile(user) : gacha.registerProfile(user);
                profile.setVoteDate(LocalDateTime.now());
                profile.getResources().addCrystals(2000);
                profile.markForSave();

                MessageChannel channel = user.getPrivateChannel().block();
                Replies.success("Thanks for voting! You can vote again in 12 hours.").create(user, channel, null);
            }

            t.sendResponseHeaders(200, 0);
        }
    }
}
