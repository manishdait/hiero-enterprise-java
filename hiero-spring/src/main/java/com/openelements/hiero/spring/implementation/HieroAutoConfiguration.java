package com.openelements.hiero.spring.implementation;

import com.hedera.hashgraph.sdk.Client;
import com.openelements.hiero.base.Account;
import com.openelements.hiero.base.AccountClient;
import com.openelements.hiero.base.AccountRepository;
import com.openelements.hiero.base.ContractVerificationClient;
import com.openelements.hiero.base.FileClient;
import com.openelements.hiero.base.NetworkRepository;
import com.openelements.hiero.base.NftClient;
import com.openelements.hiero.base.NftRepository;
import com.openelements.hiero.base.SmartContractClient;
import com.openelements.hiero.base.config.HieroConfig;
import com.openelements.hiero.base.implementation.AccountClientImpl;
import com.openelements.hiero.base.implementation.AccountRepositoryImpl;
import com.openelements.hiero.base.implementation.FileClientImpl;
import com.openelements.hiero.base.implementation.HieroNetwork;
import com.openelements.hiero.base.implementation.NetworkRepositoryImpl;
import com.openelements.hiero.base.implementation.NftClientImpl;
import com.openelements.hiero.base.implementation.NftRepositoryImpl;
import com.openelements.hiero.base.implementation.ProtocolLayerClientImpl;
import com.openelements.hiero.base.implementation.SmartContractClientImpl;
import com.openelements.hiero.base.mirrornode.MirrorNodeClient;
import com.openelements.hiero.base.protocol.ProtocolLayerClient;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@EnableConfigurationProperties({HieroProperties.class, HieroNetworkProperties.class})
public class HieroAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(HieroAutoConfiguration.class);

    @Bean
    HieroConfig hieroConfig(final HieroProperties properties) {
        return new HieroConfigImpl(properties);
    }

    @Bean
    HieroNetwork hieroNetwork(final HieroConfig hieroConfig) {
        return hieroConfig.getNetwork();
    }

    @Bean
    Account operationalAccount(final HieroConfig hieroConfig) {
        return hieroConfig.getOperatorAccount();
    }

    @Bean
    Client client(final HieroConfig hieroConfig) {
        try {
            return hieroConfig.createClient();
        } catch (Exception e) {
            throw new IllegalStateException("Can not create client", e);
        }
    }

    @Bean
    ProtocolLayerClient protocolLevelClient(final Client client, final Account operationalAccount) {
        return new ProtocolLayerClientImpl(client, operationalAccount);
    }

    @Bean
    FileClient fileClient(final ProtocolLayerClient protocolLayerClient) {
        return new FileClientImpl(protocolLayerClient);
    }

    @Bean
    SmartContractClient smartContractClient(final ProtocolLayerClient protocolLayerClient, FileClient fileClient) {
        return new SmartContractClientImpl(protocolLayerClient, fileClient);
    }

    @Bean
    AccountClient accountClient(final ProtocolLayerClient protocolLayerClient) {
        return new AccountClientImpl(protocolLayerClient);
    }

    @Bean
    NftClient nftClient(final ProtocolLayerClient protocolLayerClient, Account operationalAccount) {
        return new NftClientImpl(protocolLayerClient, operationalAccount);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.hiero", name = "mirrorNodeSupported",
            havingValue = "true", matchIfMissing = true)
    MirrorNodeClient mirrorNodeClient(final Client client, final HieroNetwork hieroNetwork) {
        final String mirrorNodeEndpoint;
        if (Objects.equals(hieroNetwork, HieroNetwork.CUSTOM)) {
            final List<String> mirrorNetwork = client.getMirrorNetwork();
            if (mirrorNetwork.isEmpty()) {
                throw new IllegalArgumentException("Mirror node endpoint must be set");
            }
            mirrorNodeEndpoint = mirrorNetwork.get(0);
        } else {
            mirrorNodeEndpoint = hieroNetwork.getMirrornodeEndpoint();
        }

        final String baseUri;
        try {
            URL url = new URI(mirrorNodeEndpoint).toURL();
            final String mirrorNodeEndpointProtocol = url.getProtocol();
            final String mirrorNodeEndpointHost = url.getHost();
            final int mirrorNodeEndpointPort;
            if (mirrorNodeEndpointProtocol == "https" && url.getPort() == -1) {
                mirrorNodeEndpointPort = 443;
            } else if (mirrorNodeEndpointProtocol == "http" && url.getPort() == -1) {
                mirrorNodeEndpointPort = 80;
            } else if (url.getPort() == -1) {
                mirrorNodeEndpointPort = 443;
            } else {
                mirrorNodeEndpointPort = url.getPort();
            }
            baseUri = mirrorNodeEndpointProtocol + "://" + mirrorNodeEndpointHost + ":" + mirrorNodeEndpointPort;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing mirrorNodeEndpoint '" + mirrorNodeEndpoint + "'", e);
        }
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUri);
        return new MirrorNodeClientImpl(builder);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.hiero", name = "mirrorNodeSupported",
            havingValue = "true", matchIfMissing = true)
    NftRepository nftRepository(final MirrorNodeClient mirrorNodeClient) {
        return new NftRepositoryImpl(mirrorNodeClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.hiero", name = "mirrorNodeSupported",
            havingValue = "true", matchIfMissing = true)
    AccountRepository accountRepository(final MirrorNodeClient mirrorNodeClient) {
        return new AccountRepositoryImpl(mirrorNodeClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.hiero", name = "mirrorNodeSupported",
            havingValue = "true", matchIfMissing = true)
    NetworkRepository networkRepository(final MirrorNodeClient mirrorNodeClient) {
        return new NetworkRepositoryImpl(mirrorNodeClient);
    }

    @Bean
    ContractVerificationClient contractVerificationClient(final HieroConfig hieroConfig) {
        return new ContractVerificationClientImplementation(hieroConfig.getNetwork());
    }
}
