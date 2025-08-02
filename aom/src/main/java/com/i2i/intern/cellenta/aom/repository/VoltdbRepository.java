package com.i2i.intern.cellenta.aom.repository;

import com.i2i.intern.cellenta.aom.dto.request.CreateBalanceRequest;
import com.i2i.intern.cellenta.aom.model.Balance;
import com.i2i.intern.cellenta.aom.model.Paket;
import com.i2i.intern.cellenta.aom.model.Voltdb.*;
import com.i2i.intern.cellenta.aom.model.Voltdb.mapper.VoltDBBalanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VoltdbRepository {

    private final RestTemplate restTemplate;
    public final String mainUrl = "http://34.10.105.56:8081/api/v1";
    private HttpHeaders headers = null;
    private final VoltDBBalanceMapper balanceMapper = new VoltDBBalanceMapper();

    private void setHeaders(){
        headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
    }

    // PACKAGE METODS --------------------------------------------

    public boolean createNewPackage(Paket paket){
        String url = mainUrl + "/package/create";
        setHeaders();
        HttpEntity<Object> request = new HttpEntity<>(
                paket,
                headers
        );
        ResponseEntity<VoltDBCreatePackageResponse> result = restTemplate.postForEntity(url, request, VoltDBCreatePackageResponse.class);
        return Objects.requireNonNull(result.getBody()).status().equals("SUCCESS");
    }

    public List<Paket> getAllPackages(){
        String url = mainUrl + "/package/list";
        setHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<VoltDBGetAllPackagesResponse> result = restTemplate.postForEntity(url, request, VoltDBGetAllPackagesResponse.class);
        return Objects.requireNonNull(result.getBody()).getResults();
    }

    // BALANCE METODS --------------------------------------------

    public boolean createNewBalance(VoltDBCreateBalanceRequest balance){
        String url = mainUrl + "/balance/create-with-defaults";
        setHeaders();
        HttpEntity<Object> request = new HttpEntity<>(
                balance,
                headers
        );
        ResponseEntity<VoltDBCreateBalanceResponse> result = restTemplate.postForEntity(url, request, VoltDBCreateBalanceResponse.class);
        System.out.println("Result.body: " + result.getBody());
        return Objects.requireNonNull(result.getBody()).status().equals("SUCCESS");
    }

    public Optional<Balance> getFirstBalance(String msisdn, Long packageId){
        String url = mainUrl + "/get";
        setHeaders();
        HttpEntity<Object> request = new HttpEntity<>(
                CreateBalanceRequest.builder().msisdn(msisdn).packageId(packageId).build(),
                headers
        );
        ResponseEntity<VoltDBGetAllBalancesResponse> response = restTemplate.postForEntity(url, request, VoltDBGetAllBalancesResponse.class);
        if(Objects.equals(Objects.requireNonNull(response.getBody()).status(), "SUCCESS")){
            return Optional.of(balanceMapper.toBalanceFromVoltDBBalanceResponse(response.getBody().results().getFirst()));
        }
        return Optional.empty();
    }

    public List<Balance> getAllBalances(String msisdn){
        String url = mainUrl + "/balance/list-by-msisdn";
        setHeaders();
        HttpEntity<Object> request = new HttpEntity<>(
                VoltDBGetAllBalancesRequest.builder().msisdn(msisdn).build(),
                headers
        );
        ResponseEntity<VoltDBGetAllBalancesResponse> response = restTemplate.postForEntity(url, request, VoltDBGetAllBalancesResponse.class);

        if (response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null
                && "SUCCESS".equals(response.getBody().status())
                && response.getBody().results() != null) {
            return response.getBody().results().stream().map(balanceMapper::toBalanceFromVoltDBBalanceResponse).toList();
        }
        return Collections.emptyList();
    }

}
