package com.project.repository;

import com.project.model.Purchase;
import java.util.List;

public interface IPurchaseRepository {
    List<Purchase> getAllPurchases();
    List<Purchase> getPurchasesBySellerID(String sellerID);
    List<Purchase> getPurchasesByBuyerID(String buyerID);
    Purchase getPurchaseByProductID(String productID);
    List<Purchase> getPurchasesByDateRangeAndBuyerID(String startDate, String endDate, String buyerID);
    List<Purchase> getPurchasesByDateRangeAndSellerID(String startDate, String endDate, String sellerID);
    List<Purchase> getPurchasesByPriceRangeAndSellerID(double minPrice, double maxPrice, String sellerID);
    List<Purchase> getPurchasesByPriceRangeAndBuyerID(double minPrice, double maxPrice, String buyerID);
    void savePurchase(Purchase purchase);
    void deletePurchase(String purchaseID);
    void updatePurchase(String purchaseID, Purchase updatedPurchase);
}