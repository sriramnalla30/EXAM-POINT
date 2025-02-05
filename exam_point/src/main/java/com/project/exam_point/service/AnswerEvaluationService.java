package com.project.exam_point.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AnswerEvaluationService {

    private final RestTemplate restTemplate;
    private final String ollamaUrl = "http://localhost:11434/api/generate";

    public AnswerEvaluationService() {
        this.restTemplate = new RestTemplate();
    }

    public boolean validateLongAnswer(String teacherAnswer, String studentAnswer) {
        // Construct the prompt template for Ollama
        String prompt = buildPrompt(teacherAnswer, studentAnswer);

        // Wait for Ollama response completion
        OllamaResponse response = waitForCompletion("llama2", prompt);

        // Parse the response and return the validation result
        if (response == null || response.getOutput() == null) {
            System.err.println("Received invalid or incomplete response from Ollama");
            throw new RuntimeException("Invalid response from Ollama");
        }

        System.out.println("Ollama Response: " + response.getOutput());
        return parseOllamaResponse(response.getOutput());
    }

    public boolean validateSimpleAnswer(String teacherAnswer, String studentAnswer) {
        // Direct comparison for MCQs and fill-in-the-blanks
        return teacherAnswer.equalsIgnoreCase(studentAnswer.trim());
    }

    private String buildPrompt(String teacherAnswer, String studentAnswer) {
        return String.format(
            "Evaluate the following based on the given criteria:\n" +
            "1. The Teacher's answer: \"%s\".\n" +
            "2. The Student's answer: \"%s\".\n" +
            "Check if the student's answer conveys the same meaning or is relevant to the teacher's answer. " +
            "If the student's answer is correct or meaningfully matches the teacher's answer, respond only with 'true'. " +
            "Otherwise, respond only with 'false'. Do not provide any explanation or additional text. Only return 'true' or 'false'.",
            teacherAnswer, studentAnswer
        );
    }

    private boolean parseOllamaResponse(String response) {
        return "true".equalsIgnoreCase(response.trim());
    }

    private OllamaResponse waitForCompletion(String model, String prompt) {
        OllamaRequest request = new OllamaRequest(model, prompt);
        OllamaResponse response;

        int maxRetries = 10;
        int retryInterval = 15000; // 15 seconds

        for (int i = 0; i < maxRetries; i++) {
            response = restTemplate.postForObject(ollamaUrl, request, OllamaResponse.class);

            if (response != null && response.isDone()) {
                return response;
            }

            try {
                Thread.sleep(retryInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for Ollama response", e);
            }
        }

        throw new RuntimeException("Ollama did not complete processing within the maximum retries");
    }

    private static class OllamaRequest {
        private String model;
        private String prompt;

        public OllamaRequest(String model, String prompt) {
            this.model = model;
            this.prompt = prompt;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }

    private static class OllamaResponse {
        private String output;
        private boolean done;

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }

        public boolean isDone() {
            return done;
        }

        public void setDone(boolean done) {
            this.done = done;
        }
    }
}
