package dk.webbies.tscreate.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collection;

/**
 * Created by erik1 on 17-06-2016.
 */
public class JSONBuilder {
    public static ObjectBuilder createObject() {
        return new ObjectBuilder();
    }

    public static JsonArray fromCollection(Collection<JsonElement> collection) {
        JsonArray result = new JsonArray();
        collection.forEach(result::add);
        return result;
    }

    public static class ObjectBuilder {
        private JsonObject obj = new JsonObject();
        public ObjectBuilder add(String key, JsonElement element) {
            assert !obj.has(key);
            obj.add(key, element);
            return this;
        }

        public ObjectBuilder add(String key, String str) {
            assert !obj.has(key);
            obj.addProperty(key, str);
            return this;
        }

        public ObjectBuilder add(String key, boolean bool) {
            assert !obj.has(key);
            obj.addProperty(key, bool);
            return this;
        }

        public ObjectBuilder add(String key, Number number) {
            assert !obj.has(key);
            obj.addProperty(key, number);
            return this;
        }

        public ObjectBuilder add(String key, char chr) {
            assert !obj.has(key);
            obj.addProperty(key, chr);
            return this;
        }

        public JsonObject build() {
            return obj;
        }
    }
}
