package com.techgear.pago.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.techgear.pago.model.Factura;
import com.techgear.pago.repository.FacturaRepository;

@Service
public class FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${usuario.service.url}")
    private String usuarioServiceUrl;

    @Value("${carro.service.url}")
    private String carroServiceUrl;

    @Value("${catalogo.service.url}")
    private String catalogoServiceUrl;

    public List<Factura> getAllFacturas(){
        return facturaRepository.findAll();
    }

    @SuppressWarnings("unchecked")
    public List<Factura> getFacturasDetalles(){
        List<Factura> facturas = facturaRepository.findAll();
        for (Factura factura : facturas) {
            try {
                String usuarioUrl = usuarioServiceUrl+"/"+factura.getUsuarioId();
                Map<String, Object> usuarioDetalles = restTemplate.getForObject(usuarioUrl, Map.class);
                factura.setUsuarioDetalles(usuarioDetalles);
                String carroUrl = carroServiceUrl+"/"+factura.getCarroId();
                Map<String, Object> carroDetalles = restTemplate.getForObject(carroUrl, Map.class);
                factura.setCarroDetalles(carroDetalles);
            } catch (Exception e) {
                factura.setUsuarioDetalles(null);
                factura.setCarroDetalles(null);
            }
        }
        return facturas;
    }

    public Factura getFactura(int id){
        return facturaRepository.findById(id).orElse(null);
    }

    public Factura saveFactura(Factura factura){
        return facturaRepository.save(factura);
    }

    public Factura updateFactura(Factura factura){
        Factura updFact = getFactura(factura.getId());
        if (updFact==null) {
            return null;
        }
        updFact.setFormaPago(factura.getFormaPago());
        updFact.setMonto(factura.getMonto());
        updFact.setFecha(factura.getFecha());
        
        return facturaRepository.save(updFact);
    }

    public void deleteFactura(Integer id){
        facturaRepository.deleteById(id);
    }

    /**
     * Procesa el pago completo con validación y actualización de stock
     */
    @Transactional
    public Factura processPaymentWithStockValidation(Factura factura) throws Exception {
        // 1. Obtener detalles del carrito
        String carroUrl = carroServiceUrl + "/carro/" + factura.getCarroId();
        ResponseEntity<Map<String, Object>> carroResponse = restTemplate.exchange(
            carroUrl, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});
        
        if (!carroResponse.getStatusCode().is2xxSuccessful() || carroResponse.getBody() == null) {
            throw new Exception("Error al obtener detalles del carrito");
        }

        Map<String, Object> carroData = carroResponse.getBody();
        
        // 2. Validar stock para todos los productos en el carrito
        if (carroData != null && carroData.containsKey("productos")) {
            Object productosObj = carroData.get("productos");
            if (productosObj instanceof List) {
                List<?> rawList = (List<?>) productosObj;
                List<Map<String, Object>> productos = new ArrayList<>();
                
                // Convertir y validar cada elemento
                for (Object item : rawList) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> mapItem = (Map<String, Object>) item;
                        productos.add(mapItem);
                    }
                }
                
                for (Map<String, Object> item : productos) {
                    Object productoObj = item.get("producto");
                    Object cantidadObj = item.get("cantidad");
                    
                    if (productoObj instanceof Map && cantidadObj instanceof Integer) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> producto = (Map<String, Object>) productoObj;
                        Integer productId = (Integer) producto.get("id");
                        Integer cantidad = (Integer) cantidadObj;
                        
                        // Validar stock llamando al microservicio catalogo
                        if (productId != null && cantidad != null && !validateStock(productId, cantidad)) {
                            throw new Exception("Stock insuficiente para producto: " + producto.get("nombre"));
                        }
                    }
                }
            }
        }

        // 3. Si todo está bien, crear la factura
        Factura savedFactura = facturaRepository.save(factura);

        // 4. Reducir stock después de crear la factura exitosamente
        try {
            if (carroData != null && carroData.containsKey("productos")) {
                Object productosObj = carroData.get("productos");
                if (productosObj instanceof List) {
                    List<?> rawList = (List<?>) productosObj;
                    List<Map<String, Object>> productos = new ArrayList<>();
                    
                    for (Object item : rawList) {
                        if (item instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> mapItem = (Map<String, Object>) item;
                            productos.add(mapItem);
                        }
                    }
                    
                    for (Map<String, Object> item : productos) {
                        Object productoObj = item.get("producto");
                        Object cantidadObj = item.get("cantidad");
                        
                        if (productoObj instanceof Map && cantidadObj instanceof Integer) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> producto = (Map<String, Object>) productoObj;
                            Integer productId = (Integer) producto.get("id");
                            Integer cantidad = (Integer) cantidadObj;
                            
                            if (productId != null && cantidad != null) {
                                reduceStock(productId, cantidad);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Si falla la reducción de stock, podríamos necesitar rollback
            // Pero por simplicidad, loggeamos el error
            System.err.println("Error al reducir stock: " + e.getMessage());
            throw e; // Re-lanzar para rollback transaccional
        }

        return savedFactura;
    }

    /**
     * Valida stock llamando al microservicio catalogo
     */
    private boolean validateStock(Integer productId, Integer quantity) {
        try {
            String stockUrl = catalogoServiceUrl + "/producto/" + productId;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                stockUrl, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> producto = response.getBody();
                
                // Verificación segura del stock
                if (producto.containsKey("stock")) {
                    Object stockObj = producto.get("stock");
                    if (stockObj instanceof Integer) {
                        Integer stock = (Integer) stockObj;
                        return stock >= quantity;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al validar stock: " + e.getMessage());
        }
        return false;
    }

    /**
     * Reduce stock llamando al microservicio catalogo
     */
    private void reduceStock(Integer productId, Integer quantity) {
        try {
            // Aquí necesitarías un endpoint PUT en catalogo para actualizar stock
            String updateStockUrl = catalogoServiceUrl + "/producto/" + productId + "/stock";
            
            Map<String, Object> updateData = Map.of("quantity", quantity);
            
            restTemplate.put(updateStockUrl, updateData);
        } catch (Exception e) {
            System.err.println("Error al reducir stock: " + e.getMessage());
            throw e; // Re-lanzar para que falle la transacción si es necesario
        }
    }
}