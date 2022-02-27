package me.gacha.gacha;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public class Emotes
{
    public static String X;

    public static void init() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode json = mapper.readValue(new File("emotes.json"), ObjectNode.class);

        X = "<:X:" + json.get("X").asText() + ">";
    }
}
