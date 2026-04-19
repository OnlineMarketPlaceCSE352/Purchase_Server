package com.project.service;

import com.project.model.Purchase;
import com.project.repository.PurchaseRepository;

import java.util.List;

public class PurchaseService {
    private final PurchaseRepository purchaseRepository = PurchaseRepository.getInstance();

    public String getAllPurchases() {
        List<Purchase> purchases = purchaseRepository.getAllPurchases();
        return getJSONArray(purchases);
    }

    public String getPurchasesBySellerID(String sellerID) {
        List<Purchase> purchases = purchaseRepository.getPurchasesBySellerID(sellerID);
        return getJSONArray(purchases);
    }

    private String getJSONArray(List<Purchase> purchases) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("[");
        for (int i = 0; i < purchases.size(); i++) {
            responseBuilder.append(purchases.get(i).toString());
            if (i < purchases.size() - 1) {
                responseBuilder.append(",");
            }
        }
        responseBuilder.append("]");
        return responseBuilder.toString();
    }
}
