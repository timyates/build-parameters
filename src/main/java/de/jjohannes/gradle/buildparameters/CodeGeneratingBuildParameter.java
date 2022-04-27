package de.jjohannes.gradle.buildparameters;

import java.util.function.Function;

import static de.jjohannes.gradle.buildparameters.Strings.capitalize;

interface CodeGeneratingBuildParameter {

    String getType();

    String getValue();

    String getSimpleName();

    static CodeGeneratingBuildParameter from(BuildParameter<?> parameter) {
        ParameterType type;
        if (parameter instanceof IntegerBuildParameter) {
            type = new ParameterType("int", "Integer", ".map(Integer::parseInt)", Function.identity());
        } else if (parameter instanceof BooleanBuildParameter) {
            type = new ParameterType("boolean", "Boolean", ".map(Boolean::parseBoolean)", Function.identity());
        } else if (parameter instanceof EnumBuildParameter) {
            String typeName = capitalize(parameter.getSimpleName());
            type = new ParameterType(typeName, typeName, ".map(" + typeName + "::valueOf)", s -> Constants.PACKAGE_NAME + "." + typeName + "." + s);
        } else {
            type = new ParameterType("String", "String", "", s -> "\"" + s + "\"");
        }

        if (parameter.getDefaultValue().isPresent()) {
            return new ParameterWithDefault(parameter, type);
        } else {
            return new ParameterWithoutDefault(parameter, type);
        }
    }

    class ParameterWithDefault implements CodeGeneratingBuildParameter {
        private final BuildParameter<?> parameter;
        private final ParameterType type;

        public ParameterWithDefault(BuildParameter<?> parameter, ParameterType type) {
            this.parameter = parameter;
            this.type = type;
        }

        @Override
        public String getType() {
            return type.name;
        }

        @Override
        public String getValue() {
            return "providers.gradleProperty(\"" + parameter.getPath() + "\")" + type.transformation + ".getOrElse(" + getDefaultValue() + ")";
        }

        private String getDefaultValue() {
            return type.defaultValueTransformation.apply(parameter.getDefaultValue().get().toString());
        }

        @Override
        public String getSimpleName() {
            return parameter.getSimpleName();
        }
    }

    class ParameterWithoutDefault implements CodeGeneratingBuildParameter {
        private final BuildParameter<?> parameter;
        private final ParameterType type;

        public ParameterWithoutDefault(BuildParameter<?> parameter, ParameterType type) {
            this.parameter = parameter;
            this.type = type;
        }

        @Override
        public String getType() {
            return "org.gradle.api.provider.Provider<" + type.typeParameter + ">";
        }

        @Override
        public String getValue() {
            return "providers.gradleProperty(\"" + parameter.getPath() + "\")" + type.transformation;
        }

        @Override
        public String getSimpleName() {
            return parameter.getSimpleName();
        }
    }

    class ParameterType {

        final String name;
        final String typeParameter;
        final String transformation;
        final Function<String, String> defaultValueTransformation;

        ParameterType(String name, String typeParameter, String transformation, Function<String, String> defaultValueTransformation) {
            this.name = name;
            this.typeParameter = typeParameter;
            this.transformation = transformation;
            this.defaultValueTransformation = defaultValueTransformation;
        }
    }
}
