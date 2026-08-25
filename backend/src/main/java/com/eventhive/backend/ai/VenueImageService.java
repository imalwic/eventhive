package com.eventhive.backend.ai;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VenueImageService {

    private final RestTemplate restTemplate = new RestTemplate();

    public byte[] generateImage(String prompt) {

        // පින්තූරයේ කොලිටි එක උපරිම කරන්න Lighting, Lenses සහ Resolution වචන එකතු කිරීම
        String enhancedPrompt = prompt + ", highly detailed, sharp focus, 8k resolution, cinematic lighting, 35mm lens, photorealistic, masterpiece";

        String formattedPrompt = enhancedPrompt.replace(" ", "%20");

        // width=1024, height=768 සහ Watermark එක අයින් කරන්න nologo=true දානවා
        String apiUrl = "https://image.pollinations.ai/prompt/" + formattedPrompt + "?width=1024&height=768&nologo=true";

        try {
            return restTemplate.getForObject(apiUrl, byte[].class);
        } catch (Exception e) {
            System.out.println("Image Generation Error: " + e.getMessage());
            return null;
        }
    }
}