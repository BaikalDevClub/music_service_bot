package ru.ivan.telegram;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

class YamlVariables {

    private Map<String, Object> variables;

    YamlVariables(String fileName){
        Yaml file = new Yaml();

        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(fileName);

        variables = file.load(inputStream);
    }

    String getVariable (String variable){
        return (String) (variables.get(variable));
    }
}
