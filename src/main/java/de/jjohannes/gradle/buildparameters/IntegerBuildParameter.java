package de.jjohannes.gradle.buildparameters;

import javax.inject.Inject;

public abstract class IntegerBuildParameter extends BuildParameter<Integer> {

    @Inject
    public IntegerBuildParameter(String name, String prefix) {
        super(name, prefix);
    }

}
