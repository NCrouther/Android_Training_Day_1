package com.bignerdranch.android.networkingarchitecture.web;

import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class VenueListDeserializer implements JsonDeserializer<VenueSearchResponse> {
    @Override
    public VenueSearchResponse deserialize(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        JsonElement responseElement = json.getAsJsonObject().get("response");
        return new Gson().fromJson(responseElement, VenueSearchResponse.class);
    }
}
