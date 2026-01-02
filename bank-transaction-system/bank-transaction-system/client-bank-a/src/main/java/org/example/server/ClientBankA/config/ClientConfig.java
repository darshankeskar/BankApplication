package org.example.server.ClientBankA.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ClientConfig {

    /**
     * Thread pool used for asynchronous forwarding of transactions
     * from Bank A client to the central server.
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService forwardExecutor() {
        // Tune this size based on expected concurrency from this client
        return Executors.newFixedThreadPool(50);
    }

    /**
     * RestTemplate backed by Apache HttpClient5 with connection pooling.
     */
    @Bean
    public RestTemplate restTemplate() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(200);

        var httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);

        return new RestTemplate(factory);
    }
}
