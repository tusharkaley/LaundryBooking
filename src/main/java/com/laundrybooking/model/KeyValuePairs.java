package com.laundrybooking.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class KeyValuePairs {
    String key;

    String value;
}
