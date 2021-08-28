package xyz.oopsjpeg.gacha.object;

import java.util.Arrays;

public class Stats
{
    private int health;
    private int defense;
    private int attack;
    private int magic;

    public Stats(int health, int defense, int attack, int magic)
    {
        this.health = health;
        this.defense = defense;
        this.attack = attack;
        this.magic = magic;
    }

    public static Stats stringToStats(String s)
    {
        int[] hdam = Arrays.stream(s.split(",")).mapToInt(Integer::parseInt).toArray();
        return new Stats(hdam[0], hdam[1], hdam[2], hdam[3]);
    }

    public int getHealth()
    {
        return health;
    }

    public int getDefense()
    {
        return defense;
    }

    public int getAttack()
    {
        return attack;
    }

    public int getMagic()
    {
        return magic;
    }

    public Stats multiply(float f)
    {
        Stats stats = new Stats(health, defense, attack, magic);
        stats.health *= 1 + f;
        stats.defense *= 1 + f;
        stats.attack *= 1 + f;
        stats.magic *= 1 + f;
        return stats;
    }
}
