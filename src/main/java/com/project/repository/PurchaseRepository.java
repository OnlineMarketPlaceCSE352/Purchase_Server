package com.project.repository;

import com.project.model.Purchase;
import com.project.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.List;

public class PurchaseRepository implements IPurchaseRepository {
    private static final class PurchaseRepositoryHolder {
        private static final PurchaseRepository instance = new PurchaseRepository();
    }

    public static PurchaseRepository getInstance() {
        return PurchaseRepositoryHolder.instance;
    }

    @Override
    public List<Purchase> getAllPurchases() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            EntityManager em = session.unwrap(EntityManager.class);
            return HibernateUtil.getJinqProvider()
                    .streamAll(em, Purchase.class)
                    .toList();
        }
    }

    @Override
    public List<Purchase> getPurchasesBySellerID(String sellerID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            EntityManager em = session.unwrap(EntityManager.class);
            return HibernateUtil.getJinqProvider()
                    .streamAll(em, Purchase.class)
                    .where(p -> p.getSellerID().equals(sellerID))
                    .toList();
        }
    }

    @Override
    public List<Purchase> getPurchasesByBuyerID(String buyerID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            EntityManager em = session.unwrap(EntityManager.class);
            return HibernateUtil.getJinqProvider()
                    .streamAll(em, Purchase.class)
                    .where(p -> p.getBuyerID().equals(buyerID))
                    .toList();
        }
    }

    @Override
    public Purchase getPurchaseByProductID(String productID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            EntityManager em = session.unwrap(EntityManager.class);
            return HibernateUtil.getJinqProvider()
                    .streamAll(em, Purchase.class)
                    .where(p -> p.getProductID().equals(productID))
                    .findFirst()
                    .orElse(null);
        }
    }

    @Override
    public List<Purchase> getPurchasesByDateRangeAndBuyerID(String startDate, String endDate, String buyerID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            EntityManager em = session.unwrap(EntityManager.class);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            return HibernateUtil.getJinqProvider()
                    .streamAll(em, Purchase.class)
                    .where(p -> p.getBuyerID().equals(buyerID)
                            && !p.getDate().before(start)
                            && !p.getDate().after(end))
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Purchase> getPurchasesByDateRangeAndSellerID(String startDate, String endDate, String sellerID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            EntityManager em = session.unwrap(EntityManager.class);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            return HibernateUtil.getJinqProvider()
                    .streamAll(em, Purchase.class)
                    .where(p -> p.getSellerID().equals(sellerID)
                            && !p.getDate().before(start)
                            && !p.getDate().after(end))
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Purchase> getPurchasesByPriceRangeAndSellerID(double minPrice, double maxPrice, String sellerID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            EntityManager em = session.unwrap(EntityManager.class);
            BigDecimal min = BigDecimal.valueOf(minPrice);
            BigDecimal max = BigDecimal.valueOf(maxPrice);

            return HibernateUtil.getJinqProvider()
                    .streamAll(em, Purchase.class)
                    .where(p -> p.getSellerID().equals(sellerID)
                            && p.getPrice().compareTo(min) >= 0
                            && p.getPrice().compareTo(max) <= 0)
                    .toList();
        }
    }

    @Override
    public List<Purchase> getPurchasesByPriceRangeAndBuyerID(double minPrice, double maxPrice, String buyerID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            EntityManager em = session.unwrap(EntityManager.class);
            BigDecimal min = BigDecimal.valueOf(minPrice);
            BigDecimal max = BigDecimal.valueOf(maxPrice);

            return HibernateUtil.getJinqProvider()
                    .streamAll(em, Purchase.class)
                    .where(p -> p.getBuyerID().equals(buyerID)
                            && p.getPrice().compareTo(min) >= 0
                            && p.getPrice().compareTo(max) <= 0)
                    .toList();
        }
    }

    public void savePurchase(Purchase purchase) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(purchase);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void deletePurchase(String purchaseID) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Purchase purchase = session.get(Purchase.class, purchaseID);
            if (purchase != null) {
                session.remove(purchase);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void updatePurchase(String purchaseID, Purchase updatedPurchase) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Purchase existing = session.get(Purchase.class, purchaseID);
            if (existing != null) {
                existing.setSellerID(updatedPurchase.getSellerID());
                existing.setBuyerID(updatedPurchase.getBuyerID());
                existing.setProductID(updatedPurchase.getProductID());
                existing.setPrice(updatedPurchase.getPrice());
                existing.setDate(updatedPurchase.getDate());
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
