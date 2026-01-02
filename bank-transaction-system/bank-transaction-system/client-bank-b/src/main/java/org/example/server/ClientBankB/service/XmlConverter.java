package org.example.server.ClientBankB.service;


import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.server.ClientBankB.xml.TransactionRequestXml;
import org.springframework.stereotype.Component;

/**
 * Converts TransactionRequestXml object to XML string using Jackson XmlMapper.
 */
@Component
public class XmlConverter {

    private final XmlMapper xmlMapper;

    public XmlConverter() {
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.registerModule(new JavaTimeModule());
        this.xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String toXml(TransactionRequestXml request) {
        try {
            return xmlMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to XML", e);
        }
    }
}
