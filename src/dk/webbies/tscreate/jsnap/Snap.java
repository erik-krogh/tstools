package dk.webbies.tscreate.jsnap;

import dk.au.cs.casa.typescript.types.Signature;
import dk.webbies.tscreate.paser.AST.FunctionExpression;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Container class that holds the classes that describes a runtime-snapshot of a piece of JavaScript code.
 */
public class Snap {
    public static class StateDump {
        Integer global;
        List<Obj> heap;
        Obj getGlobal() {
            return this.heap.get(this.global);
        }
    }

    public static class Obj extends Value {
        public int key;
        public Function function;
        public Obj env;
        public Obj prototype;
        public List<Property> properties;
        public Map<String, Property> propertyMap = null;
        public Property getProperty(String name) {
            if (properties == null) {
                return null;
            }
            return getPropertyMap().get(name);
        }

        public Map<String, Property> getPropertyMap() {
            if (propertyMap == null) {
                propertyMap = new HashMap<>();
                for (Property property : this.properties) {
                    propertyMap.put(property.name, property);
                }
            }
            return propertyMap;
        }

        public Map<String, Value> getPropertyValueMap() {
            return getPropertyMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().value));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Obj obj = (Obj) o;

            return key == obj.key;
        }


        @Override
        public int hashCode() {
            return key;
        }
    }

    public static class Property {
        public String name;
        public Boolean writeable;
        public Boolean configurable;
        public Boolean enumerable;
        public Value value;
        public Value get;
        public Value set;
    }

    public static abstract class Value {

    }

    public static class NumberConstant extends Value {
        double value;

        public NumberConstant(double value) {
            this.value = value;
        }
    }

    public static class BooleanConstant extends Value {
        boolean value;

        public BooleanConstant(boolean value) {
            this.value = value;
        }
    }

    public static class StringConstant extends Value {
        String value;
        public StringConstant(String s) {
            this.value = s;
        }
    }

    public static class Function {
        public String type;
        public String id;
        public List<Value> arguments;
        public FunctionExpression astNode;

        public List<Signature> callSignatures = new ArrayList<>();
        public List<Signature> constructorSignatures = new ArrayList<>();
    }

    public static class UndefinedConstant extends Value {

    }
}
