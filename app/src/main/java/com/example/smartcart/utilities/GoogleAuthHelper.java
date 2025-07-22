package com.example.smartcart.utilities;

import android.content.Context;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class GoogleAuthHelper {

    public static String getAccessToken(Context context) throws IOException {
        InputStream inputStream = context.getAssets().open("recommendation-shop-bd4ce03c8b6f.json");

        GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));

        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }
}
