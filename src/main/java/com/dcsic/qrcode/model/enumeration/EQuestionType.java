package com.dcsic.qrcode.model.enumeration;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum EQuestionType {

    TEXT,       // réponse libre
    NUMBER,     // réponse numérique
    CHOICE_ONE, // choix unique
    CHOICE_MULTI // choix multiples


    /*TEXT('T', "TEXT"),
    NUMBER('N', "NUMBER"),
    CHOICE('C', "CHOICE");

    protected Character value;
    protected String    label;

    EQuestionType(Character pValue, String pLabel)
    {
        this.value = pValue;
        this.label = pLabel;
    }

    public static EQuestionType getByValue(Character pValue)
    {
        return Stream.of(EQuestionType.values()).filter(val -> val.getValue().equals(pValue)).findAny().orElse(null);
    }

    Object getValue() {
        return null;
    }

    public static String[] getLabels()
    {
        return Stream.of(EQuestionType.values()).map(EQuestionType::getLabel).collect(Collectors.toList()).toArray(new String[1]);
    }

    public static String getLabelByValue(Character value)
    {
        return (String) Optional.ofNullable(getByValue(value)).map(EQuestionType::getLabel).orElse(null);
    }

    public static EQuestionType getByLabel(String label)
    {
        return Stream.of(EQuestionType.values()).filter(val -> val.getLabel().equals(label)).findAny().orElse(null);
    }

    public static Character getValueByLabel(String label)
    {
        return (Character) Optional.ofNullable(getByLabel(label)).map(EQuestionType::getValue).orElse(null);
    }

    public static List<Map<String, Object>> getLabelAsMap()
    {
        List<Map<String, Object>> details = new ArrayList<>();
        for(EQuestionType val: EQuestionType.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("value", val.getValue());
            map.put("label", val.getLabel());
            details.add(map);
        }
        return details;
    }*/
}
