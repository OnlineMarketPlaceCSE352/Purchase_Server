package com.project.service;

import com.project.model.Purchase;
import com.project.repository.PurchaseRepository;
import com.project.util.MicroserviceClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PurchaseServiceTest {
    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    private MockedStatic<MicroserviceClient> mockedClient;
    private AutoCloseable mocksCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mocksCloseable = MockitoAnnotations.openMocks(this);
        mockedClient = mockStatic(MicroserviceClient.class);

        Field repoField = PurchaseService.class.getDeclaredField("purchaseRepository");
        repoField.setAccessible(true);
        repoField.set(purchaseService, purchaseRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedClient.close();
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    void getAllPurchases_ReturnsJSONArray() {
        Purchase p1 = new Purchase("1", "seller_1", "buyer_1", "prod_1", new Date(), new BigDecimal("10.50"));
        Purchase p2 = new Purchase("2", "seller_2", "buyer_2", "prod_2", new Date(), new BigDecimal("20.75"));

        when(purchaseRepository.getAllPurchases()).thenReturn(Arrays.asList(p1, p2));

        String result = purchaseService.getAllPurchases();

        assertTrue(result.startsWith("["), "Should be a JSON Array");
        assertTrue(result.endsWith("]"), "Should be a JSON Array");
        assertTrue(result.contains("\"id\":\"1\""));
        assertTrue(result.contains("\"id\":\"2\""));
        assertTrue(result.contains("\"sellerID\":\"seller_1\""));
        assertTrue(result.contains("\"buyerID\":\"buyer_2\""));

        verify(purchaseRepository, times(1)).getAllPurchases();
    }

    @Test
    void getAllPurchases_EmptyList_ReturnsEmptyJSONArray() {
        when(purchaseRepository.getAllPurchases()).thenReturn(Collections.emptyList());

        String result = purchaseService.getAllPurchases();

        assertEquals("[]", result);
    }

    @Test
    void getPurchasesBySellerID_ReturnsJSONArray() {
        String sellerID = "seller_1";
        Purchase p1 = new Purchase("1", sellerID, "buyer_1", "prod_1", new Date(), new BigDecimal("15.00"));

        when(purchaseRepository.getPurchasesBySellerID(sellerID)).thenReturn(Collections.singletonList(p1));

        String result = purchaseService.getPurchasesBySellerID(sellerID);

        assertTrue(result.startsWith("["));
        assertTrue(result.contains("\"id\":\"1\""));
        assertTrue(result.contains("\"sellerID\":\"seller_1\""));

        verify(purchaseRepository, times(1)).getPurchasesBySellerID(sellerID);
    }

    @Test
    void getPurchasesByBuyerID_ReturnsJSONArray() {
        String buyerID = "user_1";
        Purchase p1 = new Purchase("1", "seller_1", buyerID, "prod_1", new Date(), new BigDecimal("25.00"));

        when(purchaseRepository.getPurchasesByBuyerID(buyerID)).thenReturn(Collections.singletonList(p1));

        String result = purchaseService.getPurchasesByBuyerID(buyerID);

        assertTrue(result.startsWith("["));
        assertTrue(result.contains("\"id\":\"1\""));
        assertTrue(result.contains("\"buyerID\":\"user_1\""));

        verify(purchaseRepository, times(1)).getPurchasesByBuyerID(buyerID);
    }

    @Test
    void createPurchase_SufficientFunds_SavesPurchase() {
        String incomingPurchaseJSON = "{\"productID\":\"prod_123\"}";
        String buyerID = "user_1";
        String token = "Bearer fake.token.here";

        String mockedProductJSON = "{\"id\":\"prod_123\", \"sellerID\":\"seller_5\", \"price\":50.00}";
        mockedClient.when(() -> MicroserviceClient.sendRequestToService(
                        eq(PurchaseService.PRODUCT_SERVICE_HOST),
                        eq(PurchaseService.PRODUCT_SERVICE_PORT),
                        argThat(req -> req.getPath().equals("/api/products/prod_123"))))
                .thenReturn(mockedProductJSON);

        mockedClient.when(() -> MicroserviceClient.sendRequestToService(
                        eq(PurchaseService.USER_SERVICE_HOST),
                        eq(PurchaseService.USER_SERVICE_PORT),
                        argThat(req -> req.getPath().equals("/api/users/user_1/deduct"))))
                .thenReturn("{\"success\":true}");

        purchaseService.createPurchase(incomingPurchaseJSON, buyerID, token);

        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository, times(1)).savePurchase(purchaseCaptor.capture());

        Purchase savedPurchase = purchaseCaptor.getValue();
        assertNotNull(savedPurchase.getId());
        assertEquals("prod_123", savedPurchase.getProductID());
        assertEquals("seller_5", savedPurchase.getSellerID());
        assertEquals("user_1", savedPurchase.getBuyerID());
        assertNotNull(savedPurchase.getDate());
        assertEquals(0, new BigDecimal("50.00").compareTo(new BigDecimal(savedPurchase.getPrice().toString())));
    }

    @Test
    void createPurchase_InsufficientFunds_ThrowsException() {
        String incomingPurchaseJSON = "{\"productID\":\"prod_123\"}";
        String buyerID = "user_1";
        String token = "Bearer fake.token.here";

        String mockedProductJSON = "{\"id\":\"prod_123\", \"sellerID\":\"seller_5\", \"price\":50.00}";
        mockedClient.when(() -> MicroserviceClient.sendRequestToService(
                        eq(PurchaseService.PRODUCT_SERVICE_HOST),
                        eq(PurchaseService.PRODUCT_SERVICE_PORT),
                        argThat(req -> req.getPath().equals("/api/products/prod_123"))))
                .thenReturn(mockedProductJSON);

        mockedClient.when(() -> MicroserviceClient.sendRequestToService(
                        eq(PurchaseService.USER_SERVICE_HOST),
                        eq(PurchaseService.USER_SERVICE_PORT),
                        argThat(req -> req.getPath().equals("/api/users/user_1/deduct"))))
                .thenThrow(new RuntimeException("API error: 400 Bad Request"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                purchaseService.createPurchase(incomingPurchaseJSON, buyerID, token)
        );

        assertEquals("Insufficient funds", thrown.getMessage());
        verify(purchaseRepository, never()).savePurchase(any(Purchase.class));
    }

    @Test
    void createPurchase_ProductServiceFails_ThrowsGenericException() {
        String incomingPurchaseJSON = "{\"productID\":\"prod_123\"}";
        String buyerID = "user_1";
        String token = "Bearer fake.token.here";

        mockedClient.when(() -> MicroserviceClient.sendRequestToService(
                        eq(PurchaseService.PRODUCT_SERVICE_HOST),
                        eq(PurchaseService.PRODUCT_SERVICE_PORT),
                        argThat(req -> req.getPath().equals("/api/products/prod_123"))))
                .thenThrow(new RuntimeException("Connection Refused"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                purchaseService.createPurchase(incomingPurchaseJSON, buyerID, token)
        );

        assertEquals("Connection Refused", thrown.getMessage());
        verify(purchaseRepository, never()).savePurchase(any(Purchase.class));
    }

    @Test
    void deletePurchase_CallsRepositoryDelete() {
        String purchaseId = "purchase_abc123";

        purchaseService.deletePurchase(purchaseId);

        verify(purchaseRepository, times(1)).deletePurchase(purchaseId);
    }
}