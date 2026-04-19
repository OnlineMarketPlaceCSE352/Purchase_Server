package com.project.repository;

import com.project.model.Purchase;

import java.util.List;

public class PurchaseRepository {
    private static final class PurchaseRepositoryHolder {
        private static final PurchaseRepository instance = new PurchaseRepository();
    }

    public static PurchaseRepository getInstance() {
        return PurchaseRepositoryHolder.instance;
    }

    public List<Purchase> getAllPurchases() {
        return null;
    }
    public List<Purchase> getPurchasesBySellerID(String sellerID) {
        return null;
    }
    public List<Purchase> getPurchasesByBuyerID(String buyerID) {
        return null;
    }
    public Purchase getPurchaseByProductID(String productID) {
        return null;
    }
    public List<Purchase> getPurchasesByDateRangeAndBuyerID(String startDate, String endDate, String buyerID) {
        return null;
    }
    public List<Purchase> getPurchasesByDateRangeAndSellerID(String startDate, String endDate, String sellerID) {
        return null;
    }
    public List<Purchase> getPurchasesByPriceRangeAndSellerID(double minPrice, double maxPrice, String sellerID) {
        return null;
    }
    public List<Purchase> getPurchasesByPriceRangeAndBuyerID(double minPrice, double maxPrice, String buyerID) {
        return null;
    }

    public void savePurchase(Purchase purchase) {

    }
    public void deletePurchase(String purchaseID) {

    }
    public void updatePurchase(String purchaseID, Purchase updatedPurchase) {

    }
}
