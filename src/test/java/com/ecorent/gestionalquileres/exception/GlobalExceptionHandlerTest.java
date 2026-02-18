package com.ecorent.gestionalquileres.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    GlobalExceptionHandler handler;

    @Mock
    HttpServletRequest request;

    static class Dummy {
        @SuppressWarnings("unused")
        public void dummyMethod(String param) {
            // solo para obtener un MethodParameter válido
        }
    }

    @Test
    void handleBusinessException_returnsBadRequestWithErrorResponse() {
        BusinessException ex = new BusinessException("Mensaje de negocio");
        when(request.getRequestURI()).thenReturn("/api/test");

        ResponseEntity<ErrorResponse> response =
                handler.handleBusinessException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.timestamp());
        assertTrue(body.timestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.status());
        assertEquals("Business Error", body.error());
        assertEquals("Mensaje de negocio", body.message());
        assertEquals("/api/test", body.path());
    }

    @Test
    void handleValidationException_aggregatesFieldErrorMessages() throws NoSuchMethodException {
        when(request.getRequestURI()).thenReturn("/api/validate");

        Method method = Dummy.class.getDeclaredMethod("dummyMethod", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        Object target = new Object();
        BindingResult bindingResult =
                new BeanPropertyBindingResult(target, "target");

        bindingResult.addError(new FieldError("target", "field1", null, false, null, null, "Error uno"));
        bindingResult.addError(new FieldError("target", "field2", null, false, null, null, "Error dos"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.status());
        assertEquals("Validation Error", body.error());
        assertEquals("Error uno, Error dos", body.message());
        assertEquals("/api/validate", body.path());
    }

    @Test
    void handleConstraintViolation_returnsBadRequest() {
        ConstraintViolationException ex =
                new ConstraintViolationException("Violación de restricción", null);

        when(request.getRequestURI()).thenReturn("/api/constraint");

        ResponseEntity<ErrorResponse> response =
                handler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.status());
        assertEquals("Constraint Violation", body.error());
        assertEquals("Violación de restricción", body.message());
        assertEquals("/api/constraint", body.path());
    }

    @Test
    void handleNotFound_mapsNotFoundExceptionToNotFound() {
        NotFoundException ex = new NotFoundException("Recurso no encontrado");

        when(request.getRequestURI()).thenReturn("/api/not-found");

        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.NOT_FOUND.value(), body.status());
        assertEquals("Resource Not Found", body.error());
        assertEquals("Recurso no encontrado", body.message());
        assertEquals("/api/not-found", body.path());
    }

    @Test
    void handleGeneric_returnsInternalServerErrorWithGenericMessage() {
        Exception ex = new Exception("Mensaje interno que no se expone");

        when(request.getRequestURI()).thenReturn("/api/error");

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneric(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.status());
        assertEquals("Internal Server Error", body.error());
        assertEquals("Ha ocurrido un error inesperado", body.message());
        assertEquals("/api/error", body.path());
    }
}
