package com.kosmx.emotes.main.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.dataTypes.Text;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.common.tools.Ease;
import com.kosmx.emotes.common.tools.Easing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Serialize movements as emotes from GeckoLib format
 * https://geckolib.com/
 */
public class GeckoLibSerializer {
    public static List<EmoteHolder> serialize(JsonObject node){
        if(!node.get("format_version").getAsString().equals("1.8.0")){
            EmoteInstance.instance.getLogger().log(Level.INFO, "Gecko lib format what is this version?");
        }
        return readAnimations(node.get("animations").getAsJsonObject());
    }

    private static List<EmoteHolder> readAnimations(JsonObject jsonEmotes){
        List<EmoteHolder> emotes = new ArrayList<>();
        jsonEmotes.entrySet().forEach(stringJsonElementEntry -> {
            EmoteData.EmoteBuilder builder = new EmoteData.EmoteBuilder();
            Text name = EmoteInstance.instance.getDefaults().textFromString(stringJsonElementEntry.getKey());
            JsonObject node = stringJsonElementEntry.getValue().getAsJsonObject();
            builder.endTick = (int) Math.ceil(node.get("animation_length").getAsFloat() * 20);
            if(node.has("loop")){
                builder.isLooped = node.get("loop").getAsJsonPrimitive().isBoolean() && node.get("loop").getAsBoolean();
                if(!builder.isLooped && node.get("loop").getAsJsonPrimitive().isString() && node.get("loop").getAsString().equals("hold_on_last_frame")){
                    builder.isLooped = true;
                    builder.returnTick = builder.endTick;
                }
            }
            EmoteData emoteData = builder.build();
            keyframeSerializer(emoteData, node.get("bones").getAsJsonObject());
            EmoteHolder emoteHolder = new EmoteHolder(emoteData, name, EmoteInstance.instance.getDefaults().textFromString("Imported from GeckoLib"), EmoteInstance.instance.getDefaults().emptyTex(), node.hashCode());
            emoteHolder.isFromGeckoLib = true;
            emotes.add(emoteHolder);
        });
        return emotes;
    }

    private static void keyframeSerializer(EmoteData emoteData, JsonObject node){
        if(node.has("head"))readBone(emoteData.head, node.get("head").getAsJsonObject(), emoteData);
        if(node.has("right_arm"))readBone(emoteData.rightArm, node.get("right_arm").getAsJsonObject(), emoteData);
        if(node.has("left_arm"))readBone(emoteData.leftArm, node.get("left_arm").getAsJsonObject(), emoteData);
        if(node.has("right_leg"))readBone(emoteData.rightLeg, node.get("right_leg").getAsJsonObject(), emoteData);
        if(node.has("left_leg"))readBone(emoteData.leftLeg, node.get("left_leg").getAsJsonObject(), emoteData);
        if(node.has("body"))readBone(emoteData.torso, node.get("body").getAsJsonObject(), emoteData);
    }

    private static void readBone(EmoteData.StateCollection stateCollection, JsonObject node, EmoteData emoteData){
        if(node.has("rotation")){
            JsonElement jsonRotation = node.get("rotation");
            if(jsonRotation.isJsonArray()){
                readCollection(getRots(stateCollection), 0, Ease.LINEAR, jsonRotation.getAsJsonArray(), emoteData);
            }
            else {
                jsonRotation.getAsJsonObject().entrySet().forEach(entry -> {
                    if(entry.getKey().equals("vector")){
                        readCollection(getRots(stateCollection), 0, Ease.LINEAR, entry.getValue().getAsJsonArray(), emoteData);
                    }
                    else {
                        int tick = (int) (Float.parseFloat(entry.getKey()) * 20);
                        if (entry.getValue().isJsonArray()) {
                            readCollection(getRots(stateCollection), tick, Ease.CONSTANT, entry.getValue().getAsJsonArray(), emoteData);
                        }
                        else {
                            Ease ease = Ease.LINEAR;
                            JsonObject currentNode = entry.getValue().getAsJsonObject();
                            if (currentNode.has("lerp_mode")) {
                                String lerp = currentNode.get("lerp_mode").getAsString();
                                ease = lerp.equals("catmullrom") ? Ease.INOUTSINE : Easing.easeFromString(lerp); //IDK what am I doing
                            }
                            if (currentNode.has("easing")) ease = Easing.easeFromString(currentNode.get("easing").getAsString());
                            if (currentNode.has("pre"))
                                readCollection(getRots(stateCollection), tick, ease, currentNode.get("pre").getAsJsonArray(), emoteData);
                            if (currentNode.has("vector"))
                                readCollection(getRots(stateCollection), tick, ease, currentNode.get("vector").getAsJsonArray(), emoteData);
                            if (currentNode.has("post"))
                                readCollection(getRots(stateCollection), tick, ease, currentNode.get("post").getAsJsonArray(), emoteData);
                        }
                    }
                });
            }
        }
        if(node.has("position")){
            JsonElement jsonPosition = node.get("position");
            if(jsonPosition.isJsonArray()){
                readCollection(getOffs(stateCollection), 0, Ease.LINEAR, jsonPosition.getAsJsonArray(), emoteData);
            }
            else {
                jsonPosition.getAsJsonObject().entrySet().forEach(entry -> {
                    if(entry.getKey().equals("vector")){
                        readCollection(getOffs(stateCollection), 0, Ease.LINEAR, entry.getValue().getAsJsonArray(), emoteData);
                    }else {
                        int tick = (int) (Float.parseFloat(entry.getKey()) * 20);
                        if (entry.getValue().isJsonArray()) {
                            readCollection(getOffs(stateCollection), tick, Ease.LINEAR, entry.getValue().getAsJsonArray(), emoteData);
                        }
                        else {
                            Ease ease = Ease.LINEAR;
                            JsonObject currentNode = entry.getValue().getAsJsonObject();
                            if (currentNode.has("lerp_mode")) {
                                String lerp = currentNode.get("lerp_mode").getAsString();
                                ease = lerp.equals("catmullrom") ? Ease.INOUTSINE : Easing.easeFromString(lerp); //IDK what am I doing
                            }
                            if (currentNode.has("easing")) ease = Easing.easeFromString(currentNode.get("easing").getAsString());
                            if (currentNode.has("pre"))
                                readCollection(getOffs(stateCollection), tick, ease, currentNode.get("pre").getAsJsonArray(), emoteData);
                            if (currentNode.has("vector"))
                                readCollection(getOffs(stateCollection), tick, ease, currentNode.get("vector").getAsJsonArray(), emoteData);
                            if (currentNode.has("post"))
                                readCollection(getOffs(stateCollection), tick, ease, currentNode.get("post").getAsJsonArray(), emoteData);
                        }
                    }
                });
            }
        }
    }

    private static void readCollection(EmoteData.StateCollection.State[] a, int tick, Ease ease, JsonArray array, EmoteData emoteData){
        if(a.length != 3)throw new ArrayStoreException("wrong array length");
        for(int i = 0; i < 3; i++){
            float value = array.get(i).getAsFloat();
            if(a[0] == emoteData.torso.x) value = value / 16f;
            else if(a[0] == emoteData.torso.pitch) value = -value;
            value += a[i].defaultValue;
            a[i].addKeyFrame(tick, value, ease, 0, true);
        }
    }

    private static EmoteData.StateCollection.State[] getRots(EmoteData.StateCollection stateCollection){
        return new EmoteData.StateCollection.State[] {stateCollection.pitch, stateCollection.yaw, stateCollection.roll};
    }

    private static EmoteData.StateCollection.State[] getOffs(EmoteData.StateCollection stateCollection){
        return new EmoteData.StateCollection.State[] {stateCollection.x, stateCollection.y, stateCollection.z};
    }

}
