package com.test.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ChatbotService {

    private static final String OPENAI_API_KEY = "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";
    private static final String BASE_URL = "https://api.openai.com/v1/";

    private final OkHttpClient client = new OkHttpClient();
    private String contentValue;

    public String processUserQuery(String userMessage) {
        int check=0;
        // Integrate the chatbot message sending and response handling
        System.out.println(userMessage);
        String assistantId = "asst_AFrSRl6g9yTLSIhILTnYLDOH";
        String threadId = createThread();
        check++;
        System.out.println(check+" ");
        check++;
        if (threadId == null) {
            return "Error creating thread";
        }

        addMessage(threadId, "user", userMessage);
        boolean messageAdded = addMessage(threadId, "user", userMessage);
        if (!messageAdded) {
            System.out.println("Failed to add message to thread.");
            contentValue="Failed to add message to thread.";
        }
        else{
            createAndPollRun(threadId, assistantId);
        }


        return contentValue; // This will return the chatbot's response
    }

    private String createThread() {
        JsonObject threadRequestBody = new JsonObject();
        Request request = new Request.Builder()
                .url(BASE_URL + "threads")
                .post(RequestBody.create(threadRequestBody.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .addHeader("OpenAI-Beta", "assistants=v2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JsonObject responseObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
                System.out.println("THread created successfully");
                return responseObject.get("id").getAsString();


            } else {
                System.out.println("Failed to create thread: " + response.code() + " " + response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean addMessage(String threadId, String role, String content) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("role", role);
        requestBody.addProperty("content", content);

        Request request = new Request.Builder()
                .url(BASE_URL + "threads/" + threadId + "/messages")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .addHeader("OpenAI-Beta", "assistants=v2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void createAndPollRun(String threadId, String assistantId) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("assistant_id", assistantId);

        Request request = new Request.Builder()
                .url(BASE_URL + "threads/" + threadId + "/runs")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .addHeader("OpenAI-Beta", "assistants=v2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JsonObject responseObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
                String runId = responseObject.get("id").getAsString();
                pollForRunCompletion(threadId, runId);
            } else {
                System.out.println("Failed to create run: " + response.code() + " " + response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pollForRunCompletion(String threadId, String runId) {
        String runStatus;
        int count = 2;
        System.out.print("Showing results in:");
        do {
            runStatus = getRunStatus(threadId, runId);
            try {
                Thread.sleep(2000);  // Poll every 2 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.print(" <<<" + (count--));
        } while (runStatus != null && !runStatus.equals("completed") && !runStatus.equals("failed"));

        if ("completed".equals(runStatus)) {
            fetchMessages(threadId);
        } else if ("failed".equals(runStatus)) {
            System.out.println("Run failed to complete successfully.");
        } else {
            System.out.println("Run did not complete successfully.");
        }
    }

    private String getRunStatus(String threadId, String runId) {
        Request request = new Request.Builder()
                .url(BASE_URL + "threads/" + threadId + "/runs/" + runId)
                .get()
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .addHeader("OpenAI-Beta", "assistants=v2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseText = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseText);
                return jsonResponse.getString("status");
            } else {
                System.out.println("Failed to get run status: " + response.code() + " " + response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void fetchMessages(String threadId) {
        Request request = new Request.Builder()
                .url(BASE_URL + "threads/" + threadId + "/messages")
                .get()
                .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                .addHeader("OpenAI-Beta", "assistants=v2")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);

                contentValue = jsonResponse.getJSONArray("data")
                        .getJSONObject(0)
                        .getJSONArray("content")
                        .getJSONObject(0)
                        .getJSONObject("text")
                        .getString("value");
//                contentValue="fuck up";

//                System.out.println("\nResponse Content: " + "All Good");
            } else {
                System.out.println("Failed to fetch messages: " + response.code() + " " + response.message());
                System.out.println("Response body: " + response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
