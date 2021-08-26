package xyz.oopsjpeg.gacha.object;

import xyz.oopsjpeg.gacha.CardManager;
import xyz.oopsjpeg.gacha.Gacha;
import xyz.oopsjpeg.gacha.Util;
import xyz.oopsjpeg.gacha.object.data.CardData;
import xyz.oopsjpeg.gacha.util.MultiplyComposite;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Card
{
    private final CardManager manager;
    private final CardData data;
    private final String id;

    public Card(CardManager manager, CardData data, String id)
    {
        this.manager = manager;
        this.data = data;
        this.id = id;
    }

    public CardManager getManager()
    {
        return manager;
    }

    public Gacha getGacha()
    {
        return manager.getGacha();
    }

    public CardData getData()
    {
        return data;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return data.name;
    }

    public String getVariant()
    {
        return data.variant;
    }

    public boolean hasVariant()
    {
        return data.variant != null;
    }

    public String getFullName()
    {
        return getName() + (hasVariant() ? " - " + getVariant() : "");
    }

    public BufferedImage getImage() throws IOException
    {
        return ImageIO.read(new URL(getGacha().getSettings().getDataUrl() + "\\cards\\" + getImageRaw() + ".png"));
    }

    public String getImageRaw()
    {
        return Util.toFileName(getSeries().getId() + "-" + getName() + (hasVariant() ? "-" + getVariant() : "")).toLowerCase();
    }

    public Series getSeries()
    {
        return getGacha().getAllSeries().get(data.series);
    }

    public String getSource()
    {
        return data.source;
    }

    public boolean hasSource()
    {
        return getSource() != null;
    }

    public int getTier()
    {
        return data.tier;
    }

    public boolean isDisabled()
    {
        return data.disabled;
    }

    public boolean isExclusive()
    {
        return data.exclusive;
    }

    public BufferedImage getFrame() throws IOException
    {
        return ImageIO.read(new URL(getGacha().getSettings().getDataUrl() + "\\frames\\" + getFrameRaw() + ".png"));
    }

    public String getFrameRaw()
    {
        return Util.toFileName(getSeries().getId()).toLowerCase();
    }

    public Color getFrameColor()
    {
        return getFrameColorRaw() != null ? Util.stringToColor(getFrameColorRaw()) : Color.BLACK;
    }

    public String getFrameColorRaw()
    {
        return data.frameColor;
    }

    //public Color getAltFrameColor()
    //{
    //    return getAltFrameColorRaw() != null ? Util.stringToColor(getAltFrameColorRaw()) : Color.BLACK;
    //}

    //public String getAltFrameColorRaw()
    //{
    //    return data.frameColorFlip;
    //}

    //public String getBorder()
    //{
    //    return data.border;
    //}

    //public boolean hasBorder()
    //{
    //    return getBorder() != null;
    //}

    public Color getBorderColor()
    {
        return getBorderColorRaw() != null ? Util.stringToColor(getBorderColorRaw()) : new Color(0, 0, 0, 0.6f);
    }

    public String getBorderColorRaw()
    {
        return data.borderColor;
    }

    public Font getFont() throws IOException
    {
        return Util.font(getFontRaw(), getFontSize());
    }

    public String getFontRaw()
    {
        return data.font != null ? data.font : "MERRIWEATHER";
    }

    public int getFontSize()
    {
        return data.fontSize == 0 ? Util.fontSize(getFontRaw()) : data.fontSize;
    }

    public Color getFontColor()
    {
        return getFontColorRaw() != null ? Util.stringToColor(getFontColorRaw()) : Color.WHITE;
    }

    public String getFontColorRaw()
    {
        return data.fontColor;
    }

    public Archetype getArchetype()
    {
        return data.archetype;
    }

    public Stats getStats()
    {
        return Stats.stringToStats(data.stats);
    }

    public String format()
    {
        return "[`" + String.format("%03d", Integer.parseInt(getId())) + "`] (" + Util.stars(getTier()) + ") **" + getName() + "**" + (hasVariant() ? " - " + getVariant() : "");
    }

    public String formatRaw()
    {
        return format().replaceAll("\\*", "");
    }

    public BufferedImage render() throws IOException
    {
        BufferedImage canvas = new BufferedImage(500, 680, BufferedImage.TYPE_INT_ARGB);
        BufferedImage image = getImage();
        BufferedImage frame = getFrame();
        Font font = getFont();

        // Draw the frame
        Util.drawImage(canvas, frame, 0, 0, 0, canvas.getWidth(), canvas.getHeight());
        // Color the frame
        Graphics2D g2d = canvas.createGraphics();
        g2d.setComposite(MultiplyComposite.Multiply);
        g2d.setColor(getFrameColor());
        g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw the card image
        Util.drawImage(canvas, image, 0, 16, 90, 468, 468);

        // Draw text with variant
        Color fontColor = getFontColor();
        if (hasVariant())
        {
            // Draw the card name
            Util.drawText(canvas, getName(), font, fontColor, 0, 14, 35);
            // Draw the card variant
            Util.drawText(canvas, getVariant(), font.deriveFont(18.0f), Util.tweakColorAlpha(Color.WHITE, 230), 0, 14, 67);
        }
        else
        {
            // Draw the card name
            Util.drawText(canvas, getName(), font, fontColor, 0, 14, 45);
        }

        // Draw the card stars
        Font starFont = new Font("Default", Font.BOLD, 60);
        Util.drawText(canvas, Util.stars(getTier()), starFont, fontColor, 1, (float) canvas.getWidth() / 2, canvas.getHeight() - 69);

        // Draw legend stars
        if (getTier() == 6)
        {
            Color textColorMini = Util.tweakColorAlpha(fontColor, 116);
            Font starFontMini = starFont.deriveFont(46.0f);

            Util.drawText(canvas, Util.stars(3), starFontMini, textColorMini, 1, 150, canvas.getHeight() - 69);
            Util.drawText(canvas, Util.stars(3), starFontMini, textColorMini, 1, canvas.getWidth() - 149, canvas.getHeight() - 69);
        }

        canvas.getGraphics().dispose();

        return canvas;
    }

    @Override
    public String toString()
    {
        return "Card[id=" + getId() + ", name=" + getName() + "]";
    }
}