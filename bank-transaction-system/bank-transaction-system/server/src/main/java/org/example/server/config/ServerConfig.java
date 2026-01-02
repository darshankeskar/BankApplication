package org.example.server.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server-level configuration.
 *
 * - Exposes a dedicated ExecutorService for multithreaded transaction processing.
 * - Exposes a shared XmlMapper for XML <-> Java conversion (Jackson XML).
 *
 * The ExecutorService decouples HTTP request threads from processing threads,
 * allowing controlled concurrency and back-pressure.
 */
@Configuration
public class ServerConfig {


    /**
     * XmlMapper is thread-safe once fully configured and reused for all requests.
     */
    @Bean
    public XmlMapper xmlMapper() {
        XmlMapper mapper = new XmlMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Fixed thread pool used for processing transaction requests in parallel.
     * Size is tuned for CPU cores and expected workload.
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService transactionExecutor(
            @Value("${transaction.executor.pool-size:100}") int poolSize) {
        return Executors.newFixedThreadPool(poolSize);
    }
}