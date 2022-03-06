package me.gacha.gacha.object.game;

public class Stats
{
    private int hp, def, atk, mg, spd;

    public Stats(Stats other)
    {
        this(other.hp, other.def, other.atk, other.mg, other.spd);
    }

    public Stats(int hp, int def, int atk, int mg, int spd)
    {
        this.hp = hp;
        this.def = def;
        this.atk = atk;
        this.mg = mg;
        this.spd = spd;
    }

    public Stats add(Stats other)
    {
        return new Stats(
                hp + other.hp,
                def + other.def,
                atk + other.atk,
                mg + other.mg,
                spd + other.spd);
    }

    public int getHp()
    {
        return hp;
    }

    public void setHp(int hp)
    {
        this.hp = hp;
    }

    public int getDef()
    {
        return def;
    }

    public void setDef(int def)
    {
        this.def = def;
    }

    public int getAtk()
    {
        return atk;
    }

    public void setAtk(int atk)
    {
        this.atk = atk;
    }

    public int getMg()
    {
        return mg;
    }

    public void setMg(int mg)
    {
        this.mg = mg;
    }

    public int getSpd()
    {
        return spd;
    }

    public void setSpd(int spd)
    {
        this.spd = spd;
    }

    public String print()
    {
        return hp + " HP, "
                + def + " DEF, "
                + atk + " ATK, "
                + mg + " MG, "
                + spd + " SPD";
    }
}
