package com.i2i.intern.cellenta.aom.repository;

import com.i2i.intern.cellenta.aom.model.ForgetPassword.SendCodeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
@RequiredArgsConstructor
public class ForgetPasswordRepository {

    private final RestTemplate restTemplate;
    public final String mainUrl = "http://34.133.172.73:8080";
    private HttpHeaders headers = null;

    private void setHeaders(){
        headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
    }

    public boolean sendMail(SendCodeRequest sendCodeRequest){
        String url = mainUrl + "/send-reset-code";
        setHeaders();
        HttpEntity<Object> request = new HttpEntity<>(
                sendCodeRequest,
                headers
        );
        ResponseEntity<Boolean> result = restTemplate.postForEntity(url, request, Boolean.class);
        return Boolean.TRUE.equals(result.getBody());
    }

}
