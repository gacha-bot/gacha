package me.gacha.gacha;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public class Emotes
{
    public static String FAILURE;
    public static String SUCCESS;

    public static void init() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode json = mapper.readValue(new File("emotes.json"), ObjectNode.class);

        FAILURE = json.get("FAILURE").asText();
        SUCCESS = json.get("SUCCESS").asText();
    }
}
