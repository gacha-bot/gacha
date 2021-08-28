package xyz.oopsjpeg.gacha;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.oopsjpeg.gacha.command.Replies;
import xyz.oopsjpeg.gacha.object.Vote;
import xyz.oopsjpeg.gacha.object.data.VoteData;
import xyz.oopsjpeg.gacha.object.user.Profile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;

public class ServerManager implements Manager
{
    private final Core core;
    private final int port;

    private HttpServer server;

    public ServerManager(Core core, int port)
    {
        this.core = core;
        this.port = port;
    }

    public void start() throws IOException
    {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/vote", new VoteHandler());
        server.setExecutor(null);
        server.start();

        getLogger().info("Server listening at http://localhost:" + port);
    }

    public int port()
    {
        return port;
    }

    @Override
    public Core getCore()
    {
        return core;
    }

    class VoteHandler implements HttpHandler
    {
        @Override
        public void handle(HttpExchange t) throws IOException
        {
            try (InputStreamReader isr = new InputStreamReader(t.getRequestBody());)
            {
                VoteData data = Core.GSON.fromJson(isr, VoteData.class);
                Vote vote = new Vote(core, data);
                User user = vote.getUser();

                Profile profile = core.getProfiles().get(user);
                profile.setVoteDate(LocalDateTime.now());
                profile.getResources().addCrystals(2000);
                profile.markForSave();

                MessageChannel pc = user.getPrivateChannel().block();
                pc.createMessage(Replies.success("Thanks for voting! You can vote again in 12 hours.").build()).subscribe();
            }

            t.sendResponseHeaders(200, 0);
        }
    }
}
