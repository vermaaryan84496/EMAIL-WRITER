package com.email.writer.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;


@Service

public class EmailGeneratorService {
    @Value("${gemini.api.url}")
    private String geminiapiurl;
    @Value("${gemini.api.key}")
    private String geminiapikey;
    public EmailGeneratorService(WebClient.Builder WebClientBuilder) {
        this.webclient = WebClientBuilder.build();
    }

    private final WebClient webclient;
    public String GenerateEmailReply(EmailRequest emailrequest)
    {
        String prompt=getPrompt(emailrequest);
        Map<String,Object>requestbody=Map.of(
            "contents",new Object[]{
                        Map.of("parts",new Object[]
                                {
                                    Map.of("text",prompt)
                                })

                }
        );
        String response=webclient.post()
                .uri(geminiapiurl+geminiapikey)
                .header("Content-Type","application/json")
                .bodyValue(requestbody)
                .retrieve()
                .bodyToMono(String.class)
                .block();




        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response);
            return node.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        }
        catch(Exception e){
       return "some error ocuered"+e.getMessage();
        }
    }


    private String getPrompt(EmailRequest emailrequest) {
        StringBuilder prompt=new StringBuilder();
        prompt.append("generate a proffesional email reply for the following email content. please don't generate the subject line");
        if((emailrequest.getTone()!=null) && (!emailrequest.getTone().isEmpty()))
        {
           prompt.append("Use a ").append(emailrequest.getTone()).append(" Tone");
        }
        prompt.append("/n").append("Email content:").append(emailrequest.getEmailcontent());
        return prompt.toString();
    }

}
