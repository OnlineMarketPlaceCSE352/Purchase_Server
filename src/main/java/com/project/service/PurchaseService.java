package com.project.service;

import com.project.dto.DeductRequest;
import com.project.dto.ProductDTO;
import com.project.dto.PurchaseRequest;
import com.project.dto.Request;
import com.project.mapper.DeductRequestMapper;
import com.project.mapper.ProductDTOMapper;
import com.project.mapper.PurchaseMapper;
import com.project.model.Purchase;
import com.project.repository.PurchaseRepository;
import com.project.util.Method;
import com.project.util.MicroserviceClient;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PurchaseService {
    private final PurchaseRepository purchaseRepository = PurchaseRepository.getInstance();

    public static String PRODUCT_SERVICE_HOST = "bore.pub";
    public static int PRODUCT_SERVICE_PORT = 12345;

    public static String USER_SERVICE_HOST = "bore.pub";
    public static int USER_SERVICE_PORT = 54321;

    public String getAllPurchases() {
        List<Purchase> purchases = purchaseRepository.getAllPurchases();
        return getJSONArray(purchases);
    }

    public String getPurchasesBySellerID(String sellerID) {
        List<Purchase> purchases = purchaseRepository.getPurchasesBySellerID(sellerID);
        return getJSONArray(purchases);
    }

    public String getPurchasesByBuyerID(String buyerID) {
        List<Purchase> purchases = purchaseRepository.getPurchasesByBuyerID(buyerID);
        return getJSONArray(purchases);
    }

    public void createPurchase(String purchaseJSON, String buyerID, String authHeaderToken) {
        try {
            PurchaseRequest requestDto = PurchaseMapper.mapToPurchaseRequest(purchaseJSON);

            Request productRequest = new Request(Method.GET, "/api/products/" + requestDto.getProductID());
            String productResponseJson = MicroserviceClient.sendRequestToService(
                    PRODUCT_SERVICE_HOST, PRODUCT_SERVICE_PORT, productRequest
            );
            ProductDTO product = ProductDTOMapper.mapToProductDTO(productResponseJson);

            DeductRequest deductPayload = new DeductRequest(product.getPrice());
            Request deductRequest = new Request(Method.POST, "/api/users/" + buyerID + "/deduct");

            deductRequest.addHeader("Authorization", authHeaderToken);
            deductRequest.addHeader("Content-Type", "application/json");
            deductRequest.setBody(DeductRequestMapper.mapToJSON(deductPayload));

            try {
                MicroserviceClient.sendRequestToService(USER_SERVICE_HOST, USER_SERVICE_PORT, deductRequest);
            } catch (Exception ex) {
                throw new RuntimeException("Insufficient funds");
            }

            Purchase securePurchase = new Purchase(
                    UUID.randomUUID().toString(),
                    product.getSellerID(),
                    buyerID,
                    requestDto.getProductID(),
                    new Date(),
                    product.getPrice()
            );

            purchaseRepository.savePurchase(securePurchase);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Transaction failed: " + e.getMessage());
            throw new RuntimeException("Purchase transaction failed: " + e.getMessage());
        }
    }

    public void deletePurchase(String purchaseID) {
        purchaseRepository.deletePurchase(purchaseID);
    }

    private String getJSONArray(List<Purchase> purchases) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("[");
        for (int i = 0; i < purchases.size(); i++) {
            responseBuilder.append(PurchaseMapper.mapToJSON(purchases.get(i)));
            if (i < purchases.size() - 1) {
                responseBuilder.append(",");
            }
        }
        responseBuilder.append("]");
        return responseBuilder.toString();
    }
}