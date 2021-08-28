package xyz.oopsjpeg.gacha.command;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;
import xyz.oopsjpeg.gacha.Core;
import xyz.oopsjpeg.gacha.Util;
import xyz.oopsjpeg.gacha.object.Card;
import xyz.oopsjpeg.gacha.object.user.Profile;
import xyz.oopsjpeg.gacha.object.user.ProfileCard;
import xyz.oopsjpeg.gacha.util.Constants;
import xyz.oopsjpeg.gacha.util.PagedList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

public enum Command
{
    HELP("help", "View helpful information about Gacha.")
            {
                @Override
                public Reply execute(CommandCall call)
                {
                    return new Reply().setEmbed(e -> e
                            .setTitle("Gacha Commands")
                            .setDescription(Arrays.stream(Command.values())
                                    // Sort commands by name
                                    .sorted(Comparator.comparing(Command::getName))
                                    // Format and list the commands
                                    .map(c -> "`" + call.format(c) + "` - " + c.getDescription())
                                    .collect(Collectors.joining("\n"))));
                }
            },
    PROFILE("profile", "View a profile's profile.")
            {
                @Override
                public Reply execute(CommandCall call)
                {
                    Core core = call.getCore();
                    MessageChannel channel = call.getChannel();
                    Profile profile = call.getProfile();
                    List<ProfileCard> cards = profile.getCards();

                    ProfileCard displayCard = profile.hasFavoriteCard() ? profile.getFavoriteCard() : profile.getBestCard();

                    return new Reply().setEmbed(e ->
                    {
                        e.setAuthor(profile.getUsername() + " (" + Util.stars(profile.getTier()) + ")", null, profile.getAvatarUrl());
                        e.setColor(Util.getDisplayColor(profile, channel));
                        // Display Card Thumbnail
                        if (displayCard != null)
                            e.setThumbnail(core.getSettings().getDataUrl() + "cards/images/" + displayCard.getImageRaw() + ".png");
                        // Description
                        e.setDescription(profile.getDescription());
                        // Resources
                        e.addField("Resources", Util.sticker("Crystals", Util.crystals(profile.getResources().getCrystals()))
                                + "\n" + Util.sticker("Zenith Cores", Util.zenithCores(profile.getResources().getZenithCores()))
                                + "\n" + Util.sticker("Violet Runes", Util.violetRunes(profile.getResources().getVioletRunes())), false);
                        // Cards
                        int cardsOwned = cards.size();
                        int cardsTotal = core.getCards().size();
                        float percentOwned = (float) cardsOwned / cardsTotal;
                        e.addField("Cards", Util.comma(cardsOwned) + " / " + Util.comma(cardsTotal) + " (" + Util.percent(percentOwned) + ")", true);
                        // Timelies
                        List<String> timelies = new ArrayList<>();
                        timelies.add(profile.canCollectDaily() ? "**Daily** is available." : "**Daily** is available in " + profile.timeUntilDaily());
                        timelies.add(profile.canCollectWeekly() ? "**Weekly** is available." : "**Weekly** is available in " + profile.timeUntilWeekly());
                        e.addField("Timelies", String.join("\n", timelies), true);
                        // Voting
                        if (!profile.hasVoted())
                            e.addField("Voting", "Get free rewards for voting!\nhttps://top.gg/bot/473350175000363018/vote", false);
                    });
                }
            },
    DESCRIPTION("description", "Update your profile description.")
            {
                private static final int MAX_LENGTH = 200;

                @Override
                public Reply execute(CommandCall call)
                {
                    Profile profile = call.getProfile();

                    if (!call.hasArguments())
                    {
                        if (!profile.hasDescription())
                            return Replies.failure("You don't have a profile description set.");
                        return Replies.info(profile.getDescription());
                    }

                    if (call.getArgument(0).equalsIgnoreCase("clear"))
                    {
                        if (!profile.hasDescription())
                            return Replies.failure("You don't have a profile description set.");
                        profile.clearDescription();
                        profile.markForSave();
                        return Replies.success("Your profile description has been cleared.");
                    }

                    String description = call.getRawArguments();
                    if (description.length() > MAX_LENGTH)
                        return Replies.failure("Your profile description can't be longer than **" + MAX_LENGTH + "** characters.");

                    profile.setDescription(description);
                    profile.markForSave();

                    return Replies.success("Your profile description has been set.\n\n" + description);
                }
            },
    FAVORITE("favorite", "Set your favorite card to show on your profile.")
            {
                @Override
                public Reply execute(CommandCall call)
                {
                    Profile profile = call.getProfile();

                    if (!profile.hasCards())
                        return Replies.failure("You don't have any cards.");

                    try
                    {
                        if (!call.hasArguments())
                        {
                            if (!profile.hasFavoriteCard())
                                return Replies.failure("You don't have a favorite card set.");
                            return Replies.card(profile.getFavoriteCard(), null, null);
                        }

                        if (call.getArgument(0).equalsIgnoreCase("clear"))
                        {
                            if (!profile.hasFavoriteCard())
                                return Replies.failure("You don't have a favorite card set.");
                            profile.clearFavoriteCard();
                            profile.markForSave();
                            return Replies.success("Your favorite card has been cleared.");
                        }

                        ProfileCard card = profile.findOneCard(call.getRawArguments());
                        if (card == null)
                            return Replies.failure("You either don't have that card, or it doesn't exist.");

                        profile.setFavoriteCard(card);
                        profile.markForSave();

                        return Replies.card(card, "Your favorite card has been set to **" + card.getName() + "**"
                                + (card.hasVariant() ? " " + card.getVariant() : "") + ".", null);
                    }
                    catch (IOException err)
                    {
                        err.printStackTrace();
                        return Replies.failure(err);
                    }
                }
            },
    CARD("card", "View one of your cards.")
            {
                @Override
                public Reply execute(CommandCall call)
                {
                    Profile profile = call.getProfile();

                    if (!profile.hasCards())
                        return Replies.failure("You don't have any cards.");

                    // Find the card by ID or name
                    ProfileCard card = profile.findOneCard(call.getRawArguments());
                    if (card == null)
                        return Replies.failure("You either don't have that card, or it doesn't exist.");

                    try
                    {
                        return Replies.card(card, null, null);
                    }
                    catch (IOException err)
                    {
                        err.printStackTrace();
                        return Replies.failure(err);
                    }
                }
            },
    CARDS("cards", "View your cards.")
            {
                @Override
                public Reply execute(CommandCall call)
                {
                    User user = call.getUser();
                    Core core = call.getCore();
                    Profile profile = core.getProfiles().get(user);

                    if (!profile.hasCards())
                        return Replies.failure("You don't have any cards.");

                    PagedList<ProfileCard> cards = new PagedList<>(profile.getCards(), 10);
                    // Sort cards by star, then by name
                    cards.sort(Comparator
                            .comparingInt(ProfileCard::getTier)
                            .thenComparing(ProfileCard::getLevel)
                            .reversed());

                    int cardsOwned = cards.size();

                    // Show all cards
                    if (call.getArgument(0).equalsIgnoreCase("all"))
                    {
                        try
                        {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();

                            baos.write((user.getUsername() + "'s Cards (" + Util.comma(cardsOwned) + ")"
                                    + "\n" + cards.getOriginal().stream().map(ProfileCard::formatRaw)).getBytes(StandardCharsets.UTF_8));
                            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

                            Reply result = new Reply()
                                    .setContent("Viewing all of " + Util.formatUsername(user) + "'s cards.")
                                    .setFile("Cards_" + Util.toFileName(LocalDateTime.now()) + ".txt", bais);

                            baos.close();
                            bais.close();

                            return result;
                        }
                        catch (IOException err)
                        {
                            err.printStackTrace();
                            return Replies.failure(err);
                        }
                    }

                    AtomicInteger page = new AtomicInteger(1);

                    if (Util.isDigits(call.getLastArgument()))
                        page.set(Integer.parseInt(call.getLastArgument()));

                    if (page.get() <= 0 || page.get() > cards.pages())
                        return Replies.failure("There's only " + cards.pages() + " page(s).");

                    List<ProfileCard> pagedCards = cards.page(page.get() - 1).get();
                    return new Reply().setEmbed(e -> e
                            .setAuthor(user.getUsername() + "'s Cards (" + Util.comma(cardsOwned) + ")", null, user.getAvatarUrl())
                            .setDescription(pagedCards.stream().map(ProfileCard::format).collect(Collectors.joining("\n")))
                            .setFooter("Page " + page.get() + " / " + cards.pages(), null));
                }
            },
    PULL("pull", "Pull a card.")
            {
                @Override
                public Reply execute(CommandCall call)
                {
                    Core core = call.getCore();
                    User user = call.getUser();
                    Profile profile = core.getProfiles().get(user);

                    if (profile.getResources().getCrystals() < 1000)
                        return Replies.failure("You need **" + Util.crystals(1000) + "** to pull a card.");

                    Card card = core.getCards().pullCard();
                    ProfileCard profileCard;
                    String message = null;
                    profile.getResources().subCrystals(1000);
                    boolean repull;

                    if (profile.hasCard(card))
                    {
                        profileCard = profile.getCard(card);
                        repull = true;
                    }
                    else
                    {
                        profileCard = ProfileCard.create(profile, card);
                        repull = false;
                    }

                    profile.addCard(profileCard);
                    profile.markForSave();

                    try
                    {
                        Reply reply = Replies.card(profileCard, "Pulled **" + card.getName() + "**.", message);
                        reply.setEmbed(reply.getEmbed().andThen(embed ->
                        {
                            if (repull)
                            {
                                int violetRunes = 25 * card.getTier();
                                int xp = (int) (160 + (profileCard.getMaxXp() * 0.4f));

                                profile.getResources().addVioletRunes(violetRunes);
                                profileCard.addXp(xp);
                                profileCard.handleXp();

                                embed.addField("Re-Pull", "**`+" + Util.violetRunes(violetRunes) + "`**\n**`+" + Util.xp(xp) + "`**", true);
                            }
                        }));
                        return reply;
                    }
                    catch (IOException err)
                    {
                        err.printStackTrace();
                        return Replies.failure(err);
                    }
                }
            },
    DAILY("daily", "Collect your daily reward.")
            {
                @Override
                public Reply execute(CommandCall call)
                {
                    Core core = call.getCore();
                    User user = call.getUser();
                    Profile profile = core.getProfiles().get(user);

                    if (!profile.canCollectDaily())
                        return Replies.failure("Your **Daily** is available in " + profile.timeUntilDaily() + ".");

                    int amount = Constants.TIMELY_DAY;
                    profile.getResources().addCrystals(amount);
                    profile.setDailyDate(LocalDateTime.now());
                    profile.markForSave();

                    return Replies.success("+**`" + Util.crystals(amount) + "`** from **Daily**.");
                }
            },
    WEEKLY("weekly", "Collect your weekly reward.")
            {
                @Override
                public Reply execute(CommandCall call)
                {
                    Core core = call.getCore();
                    User user = call.getUser();
                    Profile profile = core.getProfiles().get(user);

                    if (!profile.canCollectWeekly())
                        return Replies.failure("Your **Weekly** is available in " + profile.timeUntilWeekly() + ".");

                    int amount = Constants.TIMELY_WEEK;
                    profile.getResources().addCrystals(amount);
                    profile.setWeeklyDate(LocalDateTime.now());
                    profile.markForSave();

                    return Replies.success("+**`" + Util.crystals(amount) + "`** from **Weekly**.");
                }
            },
    GIVE_CRYSTALS("givecrystals", "Give a profile crystals.")
            {
                @Override
                public Reply execute(CommandCall call)
                {
                    if (!call.getUser().getId().equals(Snowflake.of(92296992004272128L)))
                        return Replies.failure("No");

                    call.getCore().getProfiles().allAsList().forEach(p ->
                    {
                        p.getResources().addCrystals(25000);
                        p.markForSave();
                    });

                    return Replies.success("Yes");
                    //User user = call.getUser();
                    //User target = call.getGateway().userById(Snowflake.of(call.getArgument(0))).block();
                    //Profile targetData = call.getCore().profile(target);
                    //int crystals = Integer.parseInt(call.getArgument(1));
//
                    //targetData.getResources().addCrystals(crystals);
                    //call.getCore().getMongo().saveProfile(targetData);
//
                    //return new Reply(call)
                    //        .embed(e -> e
                    //                .setDescription(Util.formatUsername(target) + " has received **" + Util.comma(crystals) + "** crystals."));
                }
            },
    //COUNTHEARTS("counthearts", null)
    //        {
    //            @Override
    //            public Reply execute(CommandCall call)
    //            {
    //                GatewayDiscordClient client = call.getGateway();
    //                MessageChannel channel = client.channelById(Snowflake.of(856984021078769695L)).cast(MessageChannel.class).block();
    //                List<Message> hearts = channel
    //                        .getMessagesAfter(Snowflake.of(857010045081878598L))
    //                        .collectList().block();
    //                hearts.stream()
    //                        .sorted(Comparator.comparingInt(m -> m.getReactions().isEmpty() ? 0 : m.getReactions().stream().findFirst().get().getCount()))
    //                        .forEach(m ->
    //                        {
    //                            User submitter = m.getAuthor().get();
    //                            int count = m.getReactions().isEmpty() ? 0 : m.getReactions().stream().findFirst().get().getCount();

    //                            System.out.println(submitter.username() + "#" + submitter.getDiscriminator() + " : " + (count - 1) + " votes : "
    //                                    + "https://discord.com/channels/642803460512940052/856984021078769695/" + m.getId().asString());
    //                        });
    //                return Replies.success("check console");
    //            }
    //        },
    TESTCARD("testcard", null)
            {
                @Override
                public Reply execute(CommandCall call)
                {
                    Profile profile = call.getCore().getProfiles().get(call.getUser());
                    Card card = call.getCore().getCards().findOne(call.getRawArguments());
                    try
                    {
                        return Replies.card(ProfileCard.create(profile, card), null, null);
                    }
                    catch (IOException err)
                    {
                        err.printStackTrace();
                        return Replies.failure(err);
                    }
                }
            };
    //FLIP("flip", "Flip a card over, if possible.")
    //        {
    //            @Override
    //            public Reply execute(CommandCall call)
    //            {
    //                User user = call.getUser();
    //                Gacha gacha = call.getCore();
    //                Profile profile = gacha.profiles().get(user);
//
    //                if (!profile.hasCards())
    //                    return Replies.failure("You don't have any cards.");
    //                if (!call.hasArguments())
    //                    return Replies.failure("You have to specify a card to flip over.");
//
    //                try
    //                {
    //                    ProfileCard card = profile.searchCard(call.getRawArguments());
    //                    if (card == null)
    //                        return Replies.failure("You either don't have that card, or it doesn't exist.");
    //                    if (!card.hasAltImage())
    //                        return Replies.failure("That card can't be flipped.");
//
    //                    card.setFlipped(!card.isFlipped());
//
    //                    profile.markForSave();
//
    //                    return Replies.card(card, "Flipped over **" + card.getName() + "**"
    //                            + (card.hasVariant() ? " - " + card.getVariant() : "") + ".", null);
    //                }
    //                catch (IOException error)
    //                {
    //                    return Replies.failure(error);
    //                }
    //            }
    //        };

    static
    {
        //HELP.aliases = new String[]{"?", "about"};

        //PROFILE.aliases = new String[]{"account", "bal", "balance"};

        //DESCRIPTION.aliases = new String[]{"desc", "bio"};

        //CARD.aliases = new String[]{"show", "summon", "sum"};

        //PULL.aliases = new String[]{"gacha"};

        GIVE_CRYSTALS.developerOnly = true;

        TESTCARD.developerOnly = true;
    }

    private final String name;
    private final String description;
    private boolean developerOnly;

    Command(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public abstract Reply execute(CommandCall call);

    public Reply tryExecute(CommandCall call)
    {
        User user = call.getUser();

        // Developer only
        if (developerOnly && !user.equals(call.getGateway().getApplicationInfo().flatMap(ApplicationInfo::getOwner).block()))
            return Replies.failure("You're not a developer.");

        return execute(call);
    }

    public ApplicationCommandRequest asAppCommand()
    {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description(getDescription())
                .options(getOptions())
                .build();
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public List<ApplicationCommandOptionData> getOptions()
    {
        return Collections.emptyList();
    }

    public boolean isDeveloperOnly()
    {
        return developerOnly;
    }
}
