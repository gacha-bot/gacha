package xyz.oopsjpeg.gacha.command;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import xyz.oopsjpeg.gacha.Gacha;
import xyz.oopsjpeg.gacha.Util;
import xyz.oopsjpeg.gacha.object.Banner;
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
import java.util.stream.Collectors;

/**
 * Command interface.
 * Created by oopsjpeg on 1/30/2019.
 */
public enum Command
{
    HELP("help", "View helpful information about Gacha.")
            {
                @Override
                public Reply execute(CommandCall call)
                {
                    MessageChannel channel = call.getChannel();
                    User user = call.getUser();
                    User self = call.getGateway().getSelf().block();

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
                    MessageChannel channel = call.getChannel();
                    User user = call.getUser();
                    Gacha gacha = call.getGacha();
                    Profile profile = gacha.getProfile(user);
                    List<ProfileCard> cards = profile.getCards();

                    ProfileCard displayCard = profile.hasFavoriteCard() ? profile.getFavoriteCard() : profile.getBestCard();

                    Reply result = new Reply().setEmbed(e ->
                    {
                        e.setAuthor(user.getUsername() + " (" + Util.stars(profile.getTier()) + ")", null, user.getAvatarUrl());
                        e.setColor(Util.getColor(user, channel));
                        // Display Card Thumbnail
                        if (displayCard != null)
                            e.setThumbnail(gacha.getSettings().getDataUrl() + "cards/images/" + displayCard.getCard().getImageRaw() + ".png");
                        // Description
                        e.setDescription(profile.getDescription());
                        // Resources
                        e.addField("Resources", Util.sticker("Crystals", Util.crystals(profile.getResources().getCrystals()))
                                + "\n" + Util.sticker("Zenith Cores", Util.zenithCores(profile.getResources().getZenithCores()))
                                + "\n" + Util.sticker("Violet Runes", Util.violetRunes(profile.getResources().getVioletRunes())), false);
                        // Cards
                        int cardsOwned = cards.size();
                        int cardsTotal = gacha.getCards().total();
                        float percentOwned = (float) cardsOwned / cardsTotal;
                        e.addField("Cards", Util.comma(cardsOwned) + " / " + Util.comma(cardsTotal) + " (" + Util.percent(percentOwned) + ")", true);
                        // Timelies
                        List<String> timelies = new ArrayList<>();
                        timelies.add(profile.hasDaily() ? "**Daily** is available." : "**Daily** is available in " + profile.timeUntilDaily());
                        timelies.add(profile.hasWeekly() ? "**Weekly** is available." : "**Weekly** is available in " + profile.timeUntilWeekly());
                        e.addField("Timelies", String.join("\n", timelies), true);
                        // Voting
                        if (!profile.hasVoted())
                            e.addField("Voting", "Get free rewards for voting!\nhttps://top.gg/bot/473350175000363018/vote", false);
                    });

                    return result;
                }
            },
    REGISTER("register", "Create a new Gacha profile.")
            {
                @Override
                public Reply execute(CommandCall call)
                {
                    Gacha gacha = call.getGacha();
                    MessageChannel channel = call.getChannel();
                    User user = call.getUser();

                    if (gacha.hasProfile(user))
                        return Replies.failure("You already created a profile.");

                    gacha.registerProfile(user).markForSave();

                    return new Reply().setEmbed(e -> e
                            .setTitle("You're now registered to play Gacha!")
                            .setDescription("Check your profile with `" + call.format(PROFILE) + "`."
                                    + "\nPull a new card with `" + call.format(PULL) + "`."
                                    + "\nCollect a daily reward with `" + call.format(DAILY) + "`."
                                    + "\n\nHave fun!")
                            .setColor(Util.getColor(user, channel)));
                }
            },
    DESCRIPTION("description", "Update your profile description.")
            {
                private static final int MAX_LENGTH = 200;

                @Override
                public Reply execute(CommandCall call)
                {
                    Gacha gacha = call.getGacha();
                    User user = call.getUser();
                    Profile profile = gacha.getProfile(user);

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

                    String description = call.getArgumentsRaw();
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
                    User user = call.getUser();
                    Gacha gacha = call.getGacha();
                    Profile profile = gacha.getProfile(user);

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

                        ProfileCard card = profile.searchCard(call.getArgumentsRaw());
                        if (card == null)
                            return Replies.failure("You either don't have that card, or it doesn't exist.");

                        profile.setFavoriteCard(card);
                        profile.markForSave();

                        return Replies.card(card, "Your favorite card has been set to **" + card.getName() + "**"
                                + (card.getCard().hasVariant() ? " " + card.getCard().getVariant() : "") + ".", null);
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
                    Gacha gacha = call.getGacha();
                    User user = call.getUser();
                    Profile profile = gacha.getProfile(user);

                    if (!profile.hasCards())
                        return Replies.failure("You don't have any cards.");

                    // Find the card by ID or name
                    ProfileCard card = profile.searchCard(call.getArgumentsRaw());
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
                    MessageChannel channel = call.getChannel();
                    User user = call.getUser();
                    Gacha gacha = call.getGacha();
                    Profile profile = gacha.getProfile(user);

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
                    Gacha gacha = call.getGacha();
                    User user = call.getUser();
                    Profile profile = gacha.getProfile(user);
                    Banner banner = gacha.getBanners().get("SEKAI"); // TODO Add selection when more banners are available

                    if (profile.getResources().getCrystals() < banner.getCost())
                        return Replies.failure("You need **" + Util.crystals(banner.getCost()) + "** to pull from **" + banner.getName() + "**.");

                    Card card = banner.pullCard(profile);
                    ProfileCard profileCard;
                    String message = null;
                    profile.getResources().subCrystals(banner.getCost());
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
                        Reply reply = Replies.card(profileCard, "Pulled **" + card.getName() + "** from **" + banner.getName() + "**.", message);
                        String pityT4 = (10 - profile.getBannerPityT4(banner.getId())) + " until 4*";
                        String pityT5 = (70 - profile.getBannerPityT5(banner.getId())) + " until 5*";
                        reply.setEmbed(reply.getEmbed().andThen(embed ->
                        {
                            embed.addField("Pity", pityT4 + "\n" + pityT5, true);
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
                    Gacha gacha = call.getGacha();
                    User user = call.getUser();
                    Profile profile = gacha.getProfile(user);

                    if (!profile.hasDaily())
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
                    Gacha gacha = call.getGacha();
                    User user = call.getUser();
                    Profile profile = gacha.getProfile(user);

                    if (!profile.hasWeekly())
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

                    call.getGacha().getProfilesAsList().forEach(p ->
                    {
                        p.getResources().addCrystals(25000);
                        p.markForSave();
                    });

                    return Replies.success("Yes");
                    //User user = call.getUser();
                    //User target = call.getGateway().getUserById(Snowflake.of(call.getArgument(0))).block();
                    //Profile targetData = call.getGacha().getProfile(target);
                    //int crystals = Integer.parseInt(call.getArgument(1));
//
                    //targetData.getResources().addCrystals(crystals);
                    //call.getGacha().getMongo().saveProfile(targetData);
//
                    //return new Reply(call)
                    //        .setEmbed(e -> e
                    //                .setDescription(Util.formatUsername(target) + " has received **" + Util.comma(crystals) + "** crystals."));
                }
            },
    //COUNTHEARTS("counthearts", null)
    //        {
    //            @Override
    //            public Reply execute(CommandCall call)
    //            {
    //                GatewayDiscordClient client = call.getGateway();
    //                MessageChannel channel = client.getChannelById(Snowflake.of(856984021078769695L)).cast(MessageChannel.class).block();
    //                List<Message> hearts = channel
    //                        .getMessagesAfter(Snowflake.of(857010045081878598L))
    //                        .collectList().block();
    //                hearts.stream()
    //                        .sorted(Comparator.comparingInt(m -> m.getReactions().isEmpty() ? 0 : m.getReactions().stream().findFirst().get().getCount()))
    //                        .forEach(m ->
    //                        {
    //                            User submitter = m.getAuthor().get();
    //                            int count = m.getReactions().isEmpty() ? 0 : m.getReactions().stream().findFirst().get().getCount();

    //                            System.out.println(submitter.getUsername() + "#" + submitter.getDiscriminator() + " : " + (count - 1) + " votes : "
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
                    Profile profile = call.getGacha().getProfile(call.getUser());
                    Card card = call.getGacha().getCards().findOne(call.getArgumentsRaw());
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
    //                Gacha gacha = call.getGacha();
    //                Profile profile = gacha.getProfile(user);
//
    //                if (!profile.hasCards())
    //                    return Replies.failure("You don't have any cards.");
    //                if (!call.hasArguments())
    //                    return Replies.failure("You have to specify a card to flip over.");
//
    //                try
    //                {
    //                    ProfileCard card = profile.searchCard(call.getArgumentsRaw());
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
    //                            + (card.getCard().hasVariant() ? " - " + card.getCard().getVariant() : "") + ".", null);
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
        PROFILE.registeredOnly = true;

        //DESCRIPTION.aliases = new String[]{"desc", "bio"};
        DESCRIPTION.registeredOnly = true;

        FAVORITE.registeredOnly = true;

        //CARD.aliases = new String[]{"show", "summon", "sum"};
        CARD.registeredOnly = true;

        CARDS.registeredOnly = true;

        //PULL.aliases = new String[]{"gacha"};
        PULL.registeredOnly = true;

        DAILY.registeredOnly = true;

        WEEKLY.registeredOnly = true;

        GIVE_CRYSTALS.developerOnly = true;

        TESTCARD.developerOnly = true;
        TESTCARD.registeredOnly = true;
    }

    private final String name;
    private final String description;
    private boolean developerOnly;
    private boolean registeredOnly;

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
        // Registered only
        if (registeredOnly && !call.getGacha().hasProfile(user))
            return Replies.failure("You're not registered yet. Use `" + call.format(Command.REGISTER) + "` to create a profile.");

        return execute(call);
    }

    public boolean canExecute(CommandCall call)
    {
        Gacha gacha = call.getGacha();
        User user = call.getUser();
        GatewayDiscordClient client = gacha.getGateway();
        User owner = client.getApplicationInfo().flatMap(ApplicationInfo::getOwner).block();

        return (!registeredOnly || gacha.hasProfile(user)) && (!developerOnly || user.equals(owner));
    }

    public ApplicationCommandRequest app()
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

    public boolean isRegisteredOnly()
    {
        return registeredOnly;
    }
}
