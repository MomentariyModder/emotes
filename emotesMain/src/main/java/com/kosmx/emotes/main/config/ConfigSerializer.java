package com.kosmx.emotes.main.config;

import com.google.gson.*;
import com.kosmx.emotes.common.tools.Pair;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.common.SerializableConfig;
import com.kosmx.emotes.executor.EmoteInstance;

import java.lang.reflect.Type;
import java.util.logging.Level;

public class ConfigSerializer implements JsonDeserializer<SerializableConfig>, JsonSerializer<SerializableConfig> {


    @Override
    public SerializableConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
        JsonObject node = json.getAsJsonObject();
        SerializableConfig config = EmoteInstance.instance.isClient() ? new ClientConfig() : new SerializableConfig();
        config.configVersion = SerializableConfig.staticConfigVersion;
        if(node.has("showDebug")) config.showDebug = node.get("showDebug").getAsBoolean();
        if(node.has("config_version"))config.configVersion = node.get("config_version").getAsInt();
        if(config.showDebug && config.configVersion < SerializableConfig.staticConfigVersion){
            EmoteInstance.instance.getLogger().log(Level.INFO, "Serializing config with older version.", true);
        }
        else if(config.configVersion > SerializableConfig.staticConfigVersion){
            EmoteInstance.instance.getLogger().log(Level.WARNING, "You are trying to load version "+ config.configVersion + " config. The mod can only load correctly up to v" + SerializableConfig.staticConfigVersion+". If you won't modify any config, I won't overwrite your config file.", true);
        }
        if(node.has("validate")) config.validateEmote = node.get("validate").getAsBoolean();
        if(node.has("validThreshold")) config.validThreshold = node.get("validThreshold").getAsFloat();
        if(EmoteInstance.instance.isClient()) clientDeserialize(node, (ClientConfig) config);
        return config;
    }

    private void clientDeserialize(JsonObject node, ClientConfig config){
        if(node.has("dark")) config.dark = node.get("dark").getAsBoolean();
        if(node.has("showIcon")) config.showIcons = node.get("showIcon").getAsBoolean();
        if(node.has("enablequark")) config.enableQuark = node.get("enablequark").getAsBoolean();
        if(node.has("stopThreshold")) config.stopThreshold = node.get("stopThreshold").getAsFloat();
        if(node.has("yRatio")) config.yRatio = node.get("yRatio").getAsFloat();
        if(node.has("loadBuiltin")) config.loadBuiltinEmotes = node.get("loadBuiltin").getAsBoolean();
        if(node.has("playersafety")) config.enablePlayerSafety = node.get("playersafety").getAsBoolean();
        if(node.has("perspective")) config.enablePerspective = node.get("perspective").getAsBoolean();
        if(node.has("preduxintegration")) config.perspectiveReduxIntegration = node.get("preduxintegration").getAsBoolean();
        if(node.has("fastmenu")) fastMenuDeserializer(node.get("fastmenu").getAsJsonObject(), config);
        if(node.has("keys")) keyBindsDeserializer(node.get("keys").getAsJsonArray(), config);
    }

    private void fastMenuDeserializer(JsonObject node, ClientConfig config){
        for(int i = 0; i != 8; i++){
            if(node.has(Integer.toString(i))){
                config.fastMenuHash[i] = node.get(Integer.toString(i)).getAsInt();
            }
        }
    }

    private void keyBindsDeserializer(JsonArray node, ClientConfig config){
        for(JsonElement object : node){
            JsonObject n = object.getAsJsonObject();
            config.emotesWithHash.add(new Pair<>(n.get("id").getAsInt(), n.get("key").getAsString()));
            //keyBindDeserializer(object.getAsJsonObject());
        }
    }

    @Override
    public JsonElement serialize(SerializableConfig config, Type typeOfSrc, JsonSerializationContext context){
        JsonObject node = new JsonObject();
        node.addProperty("config_version", SerializableConfig.staticConfigVersion); //I always save config with the latest format.
        node.addProperty("showDebug", config.showDebug);
        node.addProperty("validate", config.validateEmote);
        node.addProperty("validThreshold", config.validThreshold);
        if(config instanceof ClientConfig) clientSerialize((ClientConfig) config, node);
        return node;
    }

    private void clientSerialize(ClientConfig config, JsonObject node){
        node.addProperty("dark", config.dark);
        node.addProperty("enablequark", true);
        node.addProperty("showIcon", config.showIcons);
        node.addProperty("stopThreshold", config.stopThreshold);
        node.addProperty("yRatio", config.yRatio);
        node.addProperty("loadBuiltin", config.loadBuiltinEmotes);
        node.addProperty("playersafety", config.enablePlayerSafety);
        node.addProperty("perspective", config.enablePerspective);
        node.addProperty("preduxintegration", config.perspectiveReduxIntegration);
        node.add("fastmenu", fastMenuSerializer(config));
        node.add("keys", keyBindsSerializer(config));
    }

    private JsonObject fastMenuSerializer(ClientConfig config){
        JsonObject node = new JsonObject();
        for(int i = 0; i != 8; i++){
            if(config.fastMenuEmotes[i] != null){
                node.addProperty(Integer.toString(i), config.fastMenuEmotes[i].hash);
            }
        }
        return node;
    }

    private JsonArray keyBindsSerializer(ClientConfig config){
        JsonArray array = new JsonArray();
        for(EmoteHolder emote : config.emotesWithKey){
            array.add(keyBindSerializer(emote));
        }
        return array;
    }

    private JsonObject keyBindSerializer(EmoteHolder emote){
        JsonObject node = new JsonObject();
        node.addProperty("id", emote.hash);
        node.addProperty("key", emote.keyBinding.getTranslationKey());
        return node;
    }

}
