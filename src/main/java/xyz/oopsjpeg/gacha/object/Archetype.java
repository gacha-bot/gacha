package xyz.oopsjpeg.gacha.object;

public enum Archetype
{
    FIGHTER("Fighter"),
    SHREDDER("Shredder"),
    BLOCKER("Blocker"),
    EXECUTOR("Executor"),
    CASTER("Caster");

    private final String name;

    Archetype(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
