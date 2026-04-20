package com.project.controller;

import com.project.dto.Request;
import com.project.dto.Response;
import com.project.service.PurchaseService;
import com.project.util.Method;
import com.project.util.RestHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.*;
import java.net.Socket;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PurchaseControllerTest {
    private Socket mockSocket;
    private InputStream mockInputStream;
    private OutputStream mockOutputStream;
    private MockedStatic<RestHandler> mockedRestHandler;

    @BeforeEach
    void setUp() throws IOException {
        mockSocket = mock(Socket.class);
        mockInputStream = new ByteArrayInputStream(new byte[0]);
        mockOutputStream = new ByteArrayOutputStream();

        when(mockSocket.getInputStream()).thenReturn(mockInputStream);
        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);

        mockedRestHandler = mockStatic(RestHandler.class);
    }

    @AfterEach
    void tearDown() {
        mockedRestHandler.close();
    }


    private String createFakeToken(String sub, String role) {
        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"none\"}".getBytes());
        String payload = Base64.getUrlEncoder().encodeToString(
                ("{\"sub\":\"" + sub + "\", \"role\":\"" + role + "\"}").getBytes()
        );
        return "Bearer " + header + "." + payload + ".signature";
    }

    @Test
    void handleRoute_EndpointNotFound_Returns404() {
        Request mockRequest = new Request(Method.GET, "/api/unknown-endpoint");
        mockedRestHandler.when(() -> RestHandler.parseRequest(any(BufferedReader.class)))
                .thenReturn(mockRequest);

        try (MockedConstruction<PurchaseService> mockedService = mockConstruction(PurchaseService.class)) {
            PurchaseController controller = new PurchaseController(mockSocket);
            controller.run();

            ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
            mockedRestHandler.verify(() -> RestHandler.sendResponse(any(PrintWriter.class), responseCaptor.capture()));

            assertEquals(404, responseCaptor.getValue().getStatusCode());
        }
    }

    @Test
    void handleGetAllPurchases_AsAdmin_Returns200() {
        Request mockRequest = new Request(Method.GET, "/api/purchases");
        mockRequest.addHeader("Authorization", createFakeToken("admin_1", "ADMIN"));

        mockedRestHandler.when(() -> RestHandler.parseRequest(any(BufferedReader.class)))
                .thenReturn(mockRequest);

        try (MockedConstruction<PurchaseService> mockedService = mockConstruction(PurchaseService.class,
                (mock, context) -> when(mock.getAllPurchases()).thenReturn("[{\"mocked\":\"data\"}]"))) {

            PurchaseController controller = new PurchaseController(mockSocket);
            controller.run();

            ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
            mockedRestHandler.verify(() -> RestHandler.sendResponse(any(PrintWriter.class), responseCaptor.capture()));

            Response response = responseCaptor.getValue();
            assertEquals(200, response.getStatusCode());
            assertEquals("[{\"mocked\":\"data\"}]", response.getBody());
        }
    }

    @Test
    void handleGetAllPurchases_AsUser_Returns403Forbidden() {
        Request mockRequest = new Request(Method.GET, "/api/purchases");
        mockRequest.addHeader("Authorization", createFakeToken("user_1", "USER"));

        mockedRestHandler.when(() -> RestHandler.parseRequest(any(BufferedReader.class)))
                .thenReturn(mockRequest);

        try (MockedConstruction<PurchaseService> mockedService = mockConstruction(PurchaseService.class)) {
            PurchaseController controller = new PurchaseController(mockSocket);
            controller.run();

            ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
            mockedRestHandler.verify(() -> RestHandler.sendResponse(any(PrintWriter.class), responseCaptor.capture()));

            Response response = responseCaptor.getValue();
            assertEquals(403, response.getStatusCode());

            PurchaseService service = mockedService.constructed().getFirst();
            verify(service, never()).getAllPurchases();
        }
    }

    @Test
    void handleGetAllPurchases_NoToken_Returns401Unauthorized() {
        Request mockRequest = new Request(Method.GET, "/api/purchases");
        // No authorization header added

        mockedRestHandler.when(() -> RestHandler.parseRequest(any(BufferedReader.class)))
                .thenReturn(mockRequest);

        try (MockedConstruction<PurchaseService> mockedService = mockConstruction(PurchaseService.class)) {
            PurchaseController controller = new PurchaseController(mockSocket);
            controller.run();

            ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
            mockedRestHandler.verify(() -> RestHandler.sendResponse(any(PrintWriter.class), responseCaptor.capture()));

            assertEquals(401, responseCaptor.getValue().getStatusCode());
        }
    }

    @Test
    void handleCreatePurchase_AuthenticatedUser_Returns201() {
        Request mockRequest = new Request(Method.POST, "/api/purchases");
        String token = createFakeToken("user_1", "USER");
        mockRequest.addHeader("Authorization", token);
        mockRequest.setBody("{\"productID\":\"123\"}");

        mockedRestHandler.when(() -> RestHandler.parseRequest(any(BufferedReader.class)))
                .thenReturn(mockRequest);

        try (MockedConstruction<PurchaseService> mockedService = mockConstruction(PurchaseService.class)) {
            PurchaseController controller = new PurchaseController(mockSocket);
            controller.run();

            ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
            mockedRestHandler.verify(() -> RestHandler.sendResponse(any(PrintWriter.class), responseCaptor.capture()));

            Response response = responseCaptor.getValue();
            assertEquals(201, response.getStatusCode());

            PurchaseService service = mockedService.constructed().getFirst();
            verify(service, times(1)).createPurchase("{\"productID\":\"123\"}", "user_1", token);
        }
    }

    @Test
    void handleCreatePurchase_ServiceThrowsException_Returns400() {
        Request mockRequest = new Request(Method.POST, "/api/purchases");
        String token = createFakeToken("user_1", "USER");
        mockRequest.addHeader("Authorization", token);
        mockRequest.setBody("{\"productID\":\"123\"}");

        mockedRestHandler.when(() -> RestHandler.parseRequest(any(BufferedReader.class)))
                .thenReturn(mockRequest);

        try (MockedConstruction<PurchaseService> mockedService = mockConstruction(PurchaseService.class,
                (mock, context) -> doThrow(new RuntimeException("Insufficient funds")).when(mock)
                        .createPurchase(anyString(), anyString(), anyString()))) {

            PurchaseController controller = new PurchaseController(mockSocket);
            controller.run();

            ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
            mockedRestHandler.verify(() -> RestHandler.sendResponse(any(PrintWriter.class), responseCaptor.capture()));

            Response response = responseCaptor.getValue();
            assertEquals(400, response.getStatusCode());
            assertTrue(response.getBody().contains("Insufficient funds"));
        }
    }

    @Test
    void handleDeletePurchase_AsAdmin_Returns200() {
        Request mockRequest = new Request(Method.DELETE, "/api/purchases/purchase_789");
        mockRequest.addHeader("Authorization", createFakeToken("admin_1", "ADMIN"));

        mockedRestHandler.when(() -> RestHandler.parseRequest(any(BufferedReader.class)))
                .thenReturn(mockRequest);

        try (MockedConstruction<PurchaseService> mockedService = mockConstruction(PurchaseService.class)) {
            PurchaseController controller = new PurchaseController(mockSocket);
            controller.run();

            ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
            mockedRestHandler.verify(() -> RestHandler.sendResponse(any(PrintWriter.class), responseCaptor.capture()));

            assertEquals(200, responseCaptor.getValue().getStatusCode());

            PurchaseService service = mockedService.constructed().getFirst();
            verify(service, times(1)).deletePurchase("purchase_789");
        }
    }

    @Test
    void handleGetBoughtPurchases_AuthenticatedUser_Returns200() {
        Request mockRequest = new Request(Method.GET, "/api/purchases/bought");
        mockRequest.addHeader("Authorization", createFakeToken("buyer_99", "USER"));

        mockedRestHandler.when(() -> RestHandler.parseRequest(any(BufferedReader.class)))
                .thenReturn(mockRequest);

        try (MockedConstruction<PurchaseService> mockedService = mockConstruction(PurchaseService.class,
                (mock, context) -> when(mock.getPurchasesByBuyerID("buyer_99")).thenReturn("[{\"bought\":\"items\"}]"))) {

            PurchaseController controller = new PurchaseController(mockSocket);
            controller.run();

            ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
            mockedRestHandler.verify(() -> RestHandler.sendResponse(any(PrintWriter.class), responseCaptor.capture()));

            Response response = responseCaptor.getValue();
            assertEquals(200, response.getStatusCode());
            assertEquals("[{\"bought\":\"items\"}]", response.getBody());
        }
    }

    @Test
    void handleGetSoldPurchases_AuthenticatedUser_Returns200() {
        Request mockRequest = new Request(Method.GET, "/api/purchases/sold");
        mockRequest.addHeader("Authorization", createFakeToken("seller_88", "USER"));

        mockedRestHandler.when(() -> RestHandler.parseRequest(any(BufferedReader.class)))
                .thenReturn(mockRequest);

        try (MockedConstruction<PurchaseService> mockedService = mockConstruction(PurchaseService.class,
                (mock, context) -> when(mock.getPurchasesBySellerID("seller_88")).thenReturn("[{\"sold\":\"items\"}]"))) {

            PurchaseController controller = new PurchaseController(mockSocket);
            controller.run();

            ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
            mockedRestHandler.verify(() -> RestHandler.sendResponse(any(PrintWriter.class), responseCaptor.capture()));

            Response response = responseCaptor.getValue();
            assertEquals(200, response.getStatusCode());
            assertEquals("[{\"sold\":\"items\"}]", response.getBody());
        }
    }
}