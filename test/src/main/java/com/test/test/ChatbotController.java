package com.test.test;

import com.test.test.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
@RestController
@RequestMapping("/chatbot")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/query")
    public ResponseEntity<?> getResponseFromChatbot(@RequestBody MessageRequest request) {
        // Use the service to process the user's message
        int check=0;

        String query=request.getMessage();
        String responseMessage = chatbotService.processUserQuery(query);

        // Add logging to check the response
        System.out.println("Chatbot response: " + responseMessage); // Log the chatbot response

        // Return the response as a JSON object
        return ResponseEntity.ok(new MessageResponse(responseMessage));
    }

    // New /health endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Chatbot backend is running");
    }

    // DTO for receiving the message from frontend
    public static class MessageRequest {
        private String message;

        public String getMessage() {
            return message;
        }

//        public void setMessage(String message) {
//            this.message = message;
//        }
    }

    // DTO for sending the response back to the frontend
    // DTO for sending the response back to the frontend
    public static class MessageResponse {
        private String response;

        public MessageResponse(String response) {
            this.response = response;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }
    }

}
