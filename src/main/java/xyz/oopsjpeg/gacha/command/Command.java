package xyz.oopsjpeg.gacha.command;

import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import xyz.oopsjpeg.gacha.Core;
import xyz.oopsjpeg.gacha.Util;
import xyz.oopsjpeg.gacha.object.Card;
import xyz.oopsjpeg.gacha.object.Resources;
import xyz.oopsjpeg.gacha.object.user.Profile;
import xyz.oopsjpeg.gacha.object.user.ProfileCard;
import xyz.oopsjpeg.gacha.util.Constants;
import xyz.oopsjpeg.gacha.util.Embeds;
import xyz.oopsjpeg.gacha.util.PagedList;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.oopsjpeg.gacha.util.Constants.DESC_MAX_CHARS;

public enum Command
{
    HELP("help", "View helpful information about Gacha.")
            {
                @Override
                public void execute(CommandCall call)
                {
                    User u = call.getUser();
                    PrivateChannel pc = u.getPrivateChannel().block();
                    String selfId = call.getGateway().getSelfId().asString();
                    call.confirm("Check your direct messages from <@" + selfId + ">.");
                    call.reply(MessageCreateSpec.builder()
                            .addEmbed(EmbedCreateSpec.builder()
                                    .title("Gacha Commands")
                                    .description(Arrays.stream(Command.values())
                                            // Sort commands by name
                                            .sorted(Comparator.comparing(Command::getName))
                                            // Format commands
                                            .map(c -> "**`" + call.format(c) + "`**  -  " + c.getDescription())
                                            .collect(Collectors.joining("\n")))
                                    .build())
                            .build(), pc);
                }
            },
    PROFILE("profile", "View a profile's profile.")
            {
                @Override
                public void execute(CommandCall call)
                {
                    Core core = call.getCore();
                    MessageChannel channel = call.getChannel();
                    Profile profile = call.getProfile();
                    List<ProfileCard> cards = profile.getCards();

                    ProfileCard displayCard = profile.hasFavoriteCard() ? profile.getFavoriteCard() : profile.getBestCard();

                    EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                    embed.author(profile.getUsername() + " (" + Util.stars(profile.getTier()) + ")", null, profile.getAvatarUrl());
                    embed.color(Util.getDisplayColor(profile, channel));

                    // Display Card Thumbnail
                    if (displayCard != null)
                        embed.thumbnail(core.getSettings().getDataUrl() + "cards/images/" + displayCard.getImageRaw() + ".png");

                    // Description
                    embed.description(profile.getDescription());

                    // Resources
                    //@formatter:off
                    Resources resources = profile.getResources();
                    embed.addField("Resources",
                            Util.sticker("Crystals", Util.crystals(resources.getCrystals()))
                   + "\n" + Util.sticker("Zenith Cores", Util.zenithCores(resources.getZenithCores()))
                   + "\n" + Util.sticker("Violet Runes", Util.violetRunes(resources.getVioletRunes())), false);
                    //@formatter:on

                    // Cards
                    int cardsOwned = cards.size();
                    int cardsTotal = core.getCards().size();
                    float percentOwned = (float) cardsOwned / cardsTotal;
                    embed.addField("Cards", Util.comma(cardsOwned) + " / " + Util.comma(cardsTotal) + " (" + Util.percent(percentOwned) + ")", true);

                    // Timelies
                    List<String> timelies = new ArrayList<>();
                    timelies.add(profile.canCollectDaily() ? "**Daily** is available." : "**Daily** is available in " + profile.timeUntilDaily());
                    timelies.add(profile.canCollectWeekly() ? "**Weekly** is available." : "**Weekly** is available in " + profile.timeUntilWeekly());
                    embed.addField("Timelies", String.join("\n", timelies), true);

                    // Voting
                    if (!profile.hasVoted())
                        embed.addField("Voting", "Get free rewards for voting!\nhttps://top.gg/bot/473350175000363018/vote", false);

                    call.reply(MessageCreateSpec.builder()
                            .addEmbed(embed.build())
                            .build());
                }
            },
    SET_DESCRIPTION("setdesc", "Set your profile description.")
            {
                private static final int MAX_LENGTH = 200;

                @Override
                public void execute(CommandCall call)
                {
                    Profile profile = call.getProfile();
                    CommandManager manager = call.getManager();

                    if (!call.hasArguments())
                    {
                        call.reply(Replies.info("You can set your profile's description with `" + manager.format(this) + " <text>`"
                                            + "\nIt can't be longer than **" + DESC_MAX_CHARS + "** characters.")
                                .build());
                        return;
                    }

                    String description = call.getRawArguments();
                    if (description.length() > MAX_LENGTH)
                    {
                        call.reply(Replies.failure("Your description can't be longer than **" + MAX_LENGTH + "** characters.").build());
                        return;
                    }

                    profile.setDescription(description);
                    profile.markForSave();

                    call.confirm("Profile description set.");
                }
            },
    CLEAR_DESCRIPTION("cleardesc", "Clear your profile description.")
            {
                @Override
                public void execute(CommandCall call)
                {
                    Profile profile = call.getProfile();
                    profile.clearDescription();
                    profile.markForSave();
                    call.confirm("Profile description cleared.");
                }
            },
    SET_FAVORITE("setfavorite", "Set a favorite card for your profile.")
            {
                @Override
                public void execute(CommandCall call)
                {
                    Profile profile = call.getProfile();

                    if (!profile.hasCards())
                    {
                        call.reply(Replies.failure("You don't have any cards.").build());
                        return;
                    }

                    ProfileCard card = profile.findOneCard(call.getRawArguments());
                    if (card == null)
                    {
                        call.reply(Replies.failure("You either don't have that card, or it doesn't exist.").build());
                        return;
                    }

                    profile.setFavoriteCard(card);
                    profile.markForSave();

                    call.confirm("Favorite card set.");
                }
            },
    CARD("card", "View one of your cards.")
            {
                @Override
                public void execute(CommandCall call)
                {
                    Profile profile = call.getProfile();

                    if (!profile.hasCards())
                    {
                        call.reply(Replies.failure("You don't have any cards.").build());
                        return;
                    }

                    // Find the card
                    ProfileCard card = profile.findOneCard(call.getRawArguments());
                    if (card == null)
                    {
                        call.reply(Replies.failure("You either don't have that card, or it doesn't exist.").build());
                        return;
                    }

                    call.reply(MessageCreateSpec.builder()
                            .addEmbed(Embeds.card(card, profile).build())
                            .build());
                }
            },
    CARDS("cards", "View your cards.")
            {
                @Override
                public void execute(CommandCall call)
                {
                    User user = call.getUser();
                    Core core = call.getCore();
                    Profile profile = core.getProfiles().get(user);

                    if (!profile.hasCards())
                    {
                        call.reply(Replies.failure("You don't have any cards.").build());
                        return;
                    }

                    PagedList<ProfileCard> cards = new PagedList<>(profile.getCards(), 10);
                    // Sort cards by star, then by name
                    cards.sort(Comparator
                            .comparingInt(ProfileCard::getTier)
                            .thenComparing(ProfileCard::getLevel)
                            .reversed());

                    int pageNum = 1;

                    if (Util.isDigits(call.getLastArgument()))
                        pageNum = Integer.parseInt(call.getLastArgument());

                    if (pageNum <= 0 || pageNum > cards.pages())
                    {
                        call.reply(Replies.failure("There's only " + cards.pages() + " page(s).").build());
                        return;
                    }

                    List<ProfileCard> page = cards.page(pageNum - 1).get();
                    call.reply(MessageCreateSpec.builder()
                            .addEmbed(EmbedCreateSpec.builder()
                                    .author(user.getUsername() + " - Cards", null, user.getAvatarUrl())
                                    .description(page.stream().map(ProfileCard::format).collect(Collectors.joining("\n")))
                                    .footer("Page " + pageNum + " / " + cards.pages(), null)
                                    .build())
                            .build());
                }
            },
    PULL("pull", "Pull a card.")
            {
                @Override
                public void execute(CommandCall call)
                {
                    Core core = call.getCore();
                    User user = call.getUser();
                    Profile profile = core.getProfiles().get(user);
                    Resources resources = profile.getResources();

                    if (resources.getCrystals() < 1000)
                    {
                        call.reply(Replies.failure("You need **" + Util.crystals(1000) + "** to pull a card."
                                + "\nYou have **" + Util.crystals(resources.getCrystals()) + "**.").build());
                        return;
                    }

                    Card rawCard = core.getCards().pullCard();
                    boolean repull = profile.hasCard(rawCard);
                    ProfileCard card = repull ? profile.getCard(rawCard) : ProfileCard.create(profile, rawCard);

                    resources.subCrystals(1000);
                    profile.addCard(card);
                    profile.markForSave();

                    EmbedCreateSpec.Builder embed = Embeds.card(card, profile);

                    if (repull)
                    {
                        int violetRunes = 25 * card.getTier();
                        int xp = (int) (160 + (card.getMaxXp() * 0.4f));

                        resources.addVioletRunes(violetRunes);
                        card.addXp(xp);

                        embed.addField("Re-Pull", "**`+" + Util.violetRunes(violetRunes) + "`**"
                                + "\n**`+" + Util.xp(xp) + "`**", true);

                        int oldLevel = card.getLevel();
                        if (card.handleXp())
                            embed.addField("Level Up", "**`" + oldLevel + "`** > **`" + card.getLevel() + "`**", true);
                    }

                    call.reply(MessageCreateSpec.builder()
                            .addEmbed(embed.build())
                            .build());
                }
            },
    DAILY("daily", "Collect your daily reward.")
            {
                @Override
                public void execute(CommandCall call)
                {
                    Core core = call.getCore();
                    User user = call.getUser();
                    Profile profile = core.getProfiles().get(user);

                    if (!profile.canCollectDaily())
                    {
                        call.reply(Replies.failure("Your **Daily** is available in " + profile.timeUntilDaily() + ".").build());
                        return;
                    }

                    int amount = Constants.TIMELY_DAY;
                    profile.getResources().addCrystals(amount);
                    profile.setDailyDate(LocalDateTime.now());
                    profile.markForSave();

                    call.confirm("+**`" + Util.crystals(amount) + "`** from **Daily**.");
                }
            },
    WEEKLY("weekly", "Collect your weekly reward.")
            {
                @Override
                public void execute(CommandCall call)
                {
                    Core core = call.getCore();
                    User user = call.getUser();
                    Profile profile = core.getProfiles().get(user);

                    if (!profile.canCollectWeekly())
                    {
                        call.reply(Replies.failure("Your **Weekly** is available in " + profile.timeUntilWeekly() + ".").build());
                        return;
                    }

                    int amount = Constants.TIMELY_WEEK;
                    profile.getResources().addCrystals(amount);
                    profile.setWeeklyDate(LocalDateTime.now());
                    profile.markForSave();

                    call.reply(Replies.success("+**`" + Util.crystals(amount) + "`** from **Weekly**.").build());
                }
            };
    //COUNTHEARTS("counthearts", null)
    //        {
    //            @Override
    //            public void execute(CommandCall call)
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
    //                call.reply(Replies.success("check console");
    //            }
    //        },
    //TESTCARD("testcard", null)
    //        {
    //            @Override
    //            public void execute(CommandCall call)
    //            {
    //                Profile profile = call.getCore().getProfiles().get(call.getUser());
    //                Card card = call.getCore().getCards().findOne(call.getRawArguments());
    //                try
    //                {
    //                    call.reply(Replies.card(ProfileCard.create(profile, card), null, null);
    //                }
    //                catch (IOException err)
    //                {
    //                    err.printStackTrace();
    //                    call.reply(Replies.failure(err);
    //                }
    //            }
    //        };
    //FLIP("flip", "Flip a card over, if possible.")
    //        {
    //            @Override
    //            public void execute(CommandCall call)
    //            {
    //                User user = call.getUser();
    //                Gacha gacha = call.getCore();
    //                Profile profile = gacha.profiles().get(user);
//
    //                if (!profile.hasCards())
    //                    call.reply(Replies.failure("You don't have any cards.");
    //                if (!call.hasArguments())
    //                    call.reply(Replies.failure("You have to specify a card to flip over.");
//
    //                try
    //                {
    //                    ProfileCard card = profile.searchCard(call.getRawArguments());
    //                    if (card == null)
    //                        call.reply(Replies.failure("You either don't have that card, or it doesn't exist.");
    //                    if (!card.hasAltImage())
    //                        call.reply(Replies.failure("That card can't be flipped.");
//
    //                    card.setFlipped(!card.isFlipped());
//
    //                    profile.markForSave();
//
    //                    call.reply(Replies.card(card, "Flipped over **" + card.getName() + "**"
    //                            + (card.hasVariant() ? " - " + card.getVariant() : "") + ".", null);
    //                }
    //                catch (IOException error)
    //                {
    //                    call.reply(Replies.failure(error);
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
    }

    private final String name;
    private final String description;
    private boolean developerOnly;

    Command(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public abstract void execute(CommandCall call);

    public void tryExecute(CommandCall call)
    {
        User user = call.getUser();

        // Developer only
        if (developerOnly && !user.equals(call.getGateway().getApplicationInfo().flatMap(ApplicationInfo::getOwner).block()))
            Replies.failure("You're not a developer.");

        execute(call);
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
