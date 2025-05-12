package com.openelements.hiero.base.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TokenId;
import com.openelements.hiero.base.HieroException;
import com.openelements.hiero.base.data.Account;
import com.openelements.hiero.base.implementation.FungibleTokenClientImpl;
import com.openelements.hiero.base.protocol.ProtocolLayerClient;
import com.openelements.hiero.base.protocol.data.TokenCreateRequest;
import com.openelements.hiero.base.protocol.data.TokenCreateResult;
import com.openelements.hiero.base.protocol.data.TokenAssociateRequest;
import com.openelements.hiero.base.protocol.data.TokenAssociateResult;
import com.openelements.hiero.base.protocol.data.TokenDissociateRequest;
import com.openelements.hiero.base.protocol.data.TokenDissociateResult;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class FungibleClientImplTest {
    ProtocolLayerClient protocolLayerClient;
    Account operationalAccount;
    FungibleTokenClientImpl fungibleClientImpl;

    ArgumentCaptor<TokenCreateRequest> tokenCreateCaptor = ArgumentCaptor.forClass(TokenCreateRequest.class);
    ArgumentCaptor<TokenAssociateRequest> tokenAssociateCaptor = ArgumentCaptor.forClass(TokenAssociateRequest.class);
    ArgumentCaptor<TokenDissociateRequest> tokenDissociateCaptor = ArgumentCaptor.forClass(TokenDissociateRequest.class);

    @BeforeEach
    public void setup() {
        protocolLayerClient = Mockito.mock(ProtocolLayerClient.class);
        operationalAccount = Mockito.mock(Account.class);
        fungibleClientImpl = new FungibleTokenClientImpl(protocolLayerClient, operationalAccount);
    }

    @Test
    void testCreateToken() throws HieroException {
        final AccountId accountId = AccountId.fromString("1.0.0");
        final PrivateKey privateKey = PrivateKey.generateECDSA();
        final TokenId tokenId = TokenId.fromString("1.2.3");
        final TokenCreateResult tokenCreateResult = Mockito.mock(TokenCreateResult.class);

        final String name = "Fungible Token";
        final String symbol = "FT";

        when(operationalAccount.accountId()).thenReturn(accountId);
        when(operationalAccount.privateKey()).thenReturn(privateKey);
        when(protocolLayerClient.executeTokenCreateTransaction(any(TokenCreateRequest.class)))
                .thenReturn(tokenCreateResult);
        when(tokenCreateResult.tokenId()).thenReturn(tokenId);

        final TokenId result = fungibleClientImpl.createToken(name, symbol);

        verify(operationalAccount, times(1)).accountId();
        verify(operationalAccount, times(2)).privateKey(); // 1 for treasury and 1 for supply
        verify(protocolLayerClient, times(1))
                .executeTokenCreateTransaction(tokenCreateCaptor.capture());
        verify(tokenCreateResult, times(1)).tokenId();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(tokenId, result);

        TokenCreateRequest createRequest = tokenCreateCaptor.getValue();

        Assertions.assertEquals(accountId, createRequest.treasuryAccountId());
        Assertions.assertEquals(privateKey, createRequest.treasuryKey());
        Assertions.assertEquals(privateKey, createRequest.supplyKey());
        Assertions.assertEquals(0, createRequest.initialSupply());
        Assertions.assertEquals(name, createRequest.name());
        Assertions.assertEquals(symbol, createRequest.symbol());
    }

    @Test
    void testCreateTokenWithInitialSupply() throws HieroException {
        final AccountId accountId = AccountId.fromString("1.0.0");
        final PrivateKey privateKey = PrivateKey.generateECDSA();
        final TokenId tokenId = TokenId.fromString("1.2.3");
        final TokenCreateResult tokenCreateResult = Mockito.mock(TokenCreateResult.class);

        final String name = "Fungible Token";
        final String symbol = "FT";
        final long initialSupply = 10;

        when(operationalAccount.accountId()).thenReturn(accountId);
        when(operationalAccount.privateKey()).thenReturn(privateKey);
        when(protocolLayerClient.executeTokenCreateTransaction(any(TokenCreateRequest.class)))
                .thenReturn(tokenCreateResult);
        when(tokenCreateResult.tokenId()).thenReturn(tokenId);

        final TokenId result = fungibleClientImpl.createToken(name, symbol, initialSupply);

        verify(operationalAccount, times(1)).accountId();
        verify(operationalAccount, times(2)).privateKey(); // 1 for treasury and 1 for supply
        verify(protocolLayerClient, times(1))
                .executeTokenCreateTransaction(tokenCreateCaptor.capture());
        verify(tokenCreateResult, times(1)).tokenId();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(tokenId, result);

        TokenCreateRequest createRequest = tokenCreateCaptor.getValue();

        Assertions.assertEquals(accountId, createRequest.treasuryAccountId());
        Assertions.assertEquals(privateKey, createRequest.treasuryKey());
        Assertions.assertEquals(privateKey, createRequest.supplyKey());
        Assertions.assertEquals(initialSupply, createRequest.initialSupply());
        Assertions.assertEquals(name, createRequest.name());
        Assertions.assertEquals(symbol, createRequest.symbol());
    }

    @Test
    void testCreateTokenWithSupplyKey() throws HieroException {
        final AccountId accountId = AccountId.fromString("1.0.0");
        final PrivateKey privateKey = PrivateKey.generateECDSA();
        final TokenId tokenId = TokenId.fromString("1.2.3");
        final TokenCreateResult tokenCreateResult = Mockito.mock(TokenCreateResult.class);

        final String name = "Fungible Token";
        final String symbol = "FT";
        final PrivateKey supplyKey = PrivateKey.generateECDSA();

        when(operationalAccount.accountId()).thenReturn(accountId);
        when(operationalAccount.privateKey()).thenReturn(privateKey);
        when(protocolLayerClient.executeTokenCreateTransaction(any(TokenCreateRequest.class)))
                .thenReturn(tokenCreateResult);
        when(tokenCreateResult.tokenId()).thenReturn(tokenId);

        final TokenId result = fungibleClientImpl.createToken(name, symbol, supplyKey);

        verify(operationalAccount, times(1)).accountId();
        verify(operationalAccount, times(1)).privateKey();
        verify(protocolLayerClient, times(1))
                .executeTokenCreateTransaction(tokenCreateCaptor.capture());
        verify(tokenCreateResult, times(1)).tokenId();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(tokenId, result);

        TokenCreateRequest createRequest = tokenCreateCaptor.getValue();

        Assertions.assertEquals(accountId, createRequest.treasuryAccountId());
        Assertions.assertEquals(privateKey, createRequest.treasuryKey());
        Assertions.assertEquals(supplyKey, createRequest.supplyKey());
        Assertions.assertEquals(0, createRequest.initialSupply());
        Assertions.assertEquals(name, createRequest.name());
        Assertions.assertEquals(symbol, createRequest.symbol());
    }

    @Test
    void testCreateTokenWithAccount() throws HieroException {
        final PrivateKey operatorKey = PrivateKey.generateECDSA();
        final AccountId accountId = AccountId.fromString("2.0.0");
        final PrivateKey privateKey = PrivateKey.generateECDSA();

        final TokenId tokenId = TokenId.fromString("1.2.3");
        final TokenCreateResult tokenCreateResult = Mockito.mock(TokenCreateResult.class);

        final String name = "Fungible Token";
        final String symbol = "FT";
        final Account account = Mockito.mock(Account.class);

        when(operationalAccount.privateKey()).thenReturn(operatorKey);
        when(account.accountId()).thenReturn(accountId);
        when(account.privateKey()).thenReturn(privateKey);
        when(protocolLayerClient.executeTokenCreateTransaction(any(TokenCreateRequest.class)))
                .thenReturn(tokenCreateResult);
        when(tokenCreateResult.tokenId()).thenReturn(tokenId);

        final TokenId result = fungibleClientImpl.createToken(name, symbol, account);

        verify(operationalAccount, times(1)).privateKey();
        verify(account, times(1)).accountId();
        verify(account, times(1)).privateKey();
        verify(protocolLayerClient, times(1))
                .executeTokenCreateTransaction(tokenCreateCaptor.capture());
        verify(tokenCreateResult, times(1)).tokenId();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(tokenId, result);

        TokenCreateRequest createRequest = tokenCreateCaptor.getValue();

        Assertions.assertEquals(accountId, createRequest.treasuryAccountId());
        Assertions.assertEquals(privateKey, createRequest.treasuryKey());
        Assertions.assertEquals(operatorKey, createRequest.supplyKey());
        Assertions.assertEquals(0, createRequest.initialSupply());
        Assertions.assertEquals(name, createRequest.name());
        Assertions.assertEquals(symbol, createRequest.symbol());
    }

    @Test
    void testAssociateToken() throws HieroException {
        final TokenAssociateResult tokenAssociateResult = Mockito.mock(TokenAssociateResult.class);

        final TokenId tokenId = TokenId.fromString("1.2.3");
        final AccountId accountId = AccountId.fromString("1.2.3");
        final PrivateKey accountKey = PrivateKey.generateECDSA();

        when(protocolLayerClient.executeTokenAssociateTransaction(any(TokenAssociateRequest.class)))
                .thenReturn(tokenAssociateResult);

        fungibleClientImpl.associateToken(tokenId, accountId, accountKey);

        verify(protocolLayerClient, times(1))
                .executeTokenAssociateTransaction(tokenAssociateCaptor.capture());

        final TokenAssociateRequest request = tokenAssociateCaptor.getValue();
        Assertions.assertEquals(List.of(tokenId), request.tokenIds());
        Assertions.assertEquals(accountId, request.accountId());
        Assertions.assertEquals(accountKey, request.accountPrivateKey());
    }

    @Test
    void testAssociateTokenWithAccount() throws HieroException {
        final TokenAssociateResult tokenAssociateResult = Mockito.mock(TokenAssociateResult.class);
        final AccountId accountId = AccountId.fromString("1.2.3");
        final PrivateKey privateKey = PrivateKey.generateECDSA();
        final PublicKey publicKey = privateKey.getPublicKey();

        final TokenId tokenId = TokenId.fromString("1.2.3");
        final Account account = new Account(accountId, publicKey, privateKey);

        when(protocolLayerClient.executeTokenAssociateTransaction(any(TokenAssociateRequest.class)))
                .thenReturn(tokenAssociateResult);

        fungibleClientImpl.associateToken(tokenId, account);

        verify(protocolLayerClient, times(1))
                .executeTokenAssociateTransaction(tokenAssociateCaptor.capture());

        final TokenAssociateRequest request = tokenAssociateCaptor.getValue();
        Assertions.assertEquals(List.of(tokenId), request.tokenIds());
        Assertions.assertEquals(accountId, request.accountId());
        Assertions.assertEquals(privateKey, request.accountPrivateKey());
    }

    @Test
    void testAssociateTokenThrowsExceptionForInvalidId() throws HieroException {
        final TokenId tokenId = TokenId.fromString("1.2.3");
        final AccountId accountId = AccountId.fromString("1.2.3");
        final PrivateKey accountKey = PrivateKey.generateECDSA();
        final Account account = new Account(accountId, accountKey.getPublicKey(), accountKey);

        when(protocolLayerClient.executeTokenAssociateTransaction(any(TokenAssociateRequest.class)))
                .thenThrow(new HieroException("Failed to execute transaction of type TokenAssociateTransaction"));

        Assertions.assertThrows(HieroException.class, () -> fungibleClientImpl.associateToken(tokenId, accountId, accountKey));
        Assertions.assertThrows(HieroException.class, () -> fungibleClientImpl.associateToken(tokenId, account));
    }

    @Test
    void testAssociateTokenNullParam() {
        Assertions.assertThrows(NullPointerException.class,
                () -> fungibleClientImpl.associateToken((TokenId) null, (AccountId) null, (PrivateKey) null));
        Assertions.assertThrows(NullPointerException.class,
                () -> fungibleClientImpl.associateToken((TokenId) null, null));
    }

    @Test
    void testAssociateTokenWithMultipleToken() throws HieroException {
        final TokenAssociateResult tokenAssociateResult = Mockito.mock(TokenAssociateResult.class);

        final TokenId tokenId1 = TokenId.fromString("1.2.3");
        final TokenId tokenId2 = TokenId.fromString("1.2.4");

        final AccountId accountId = AccountId.fromString("1.2.3");
        final PrivateKey accountKey = PrivateKey.generateECDSA();

        when(protocolLayerClient.executeTokenAssociateTransaction(any(TokenAssociateRequest.class)))
                .thenReturn(tokenAssociateResult);

        fungibleClientImpl.associateToken(List.of(tokenId1, tokenId2), accountId, accountKey);

        verify(protocolLayerClient, times(1))
                .executeTokenAssociateTransaction(tokenAssociateCaptor.capture());

        final TokenAssociateRequest request = tokenAssociateCaptor.getValue();
        Assertions.assertEquals(List.of(tokenId1, tokenId2), request.tokenIds());
        Assertions.assertEquals(accountId, request.accountId());
        Assertions.assertEquals(accountKey, request.accountPrivateKey());
    }

    @Test
    void testAssociateTokenWithMultipleTokenThrowExceptionIfListEmpty() {
        final AccountId accountId = AccountId.fromString("1.2.3");
        final PrivateKey accountKey = PrivateKey.generateECDSA();

        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> fungibleClientImpl.associateToken(List.of(), accountId, accountKey));
        Assertions.assertEquals("tokenIds must not be empty", e.getMessage());
    }

    @Test
    void testDissociateToken() throws HieroException {
        final TokenDissociateResult tokenDissociateResult = Mockito.mock(TokenDissociateResult.class);

        final TokenId tokenId = TokenId.fromString("1.2.3");
        final AccountId accountId = AccountId.fromString("1.2.3");
        final PrivateKey accountKey = PrivateKey.generateECDSA();

        when(protocolLayerClient.executeTokenDissociateTransaction(any(TokenDissociateRequest.class)))
                .thenReturn(tokenDissociateResult);

       fungibleClientImpl.dissociateToken(tokenId, accountId, accountKey);

        verify(protocolLayerClient, times(1))
                .executeTokenDissociateTransaction(tokenDissociateCaptor.capture());

        final TokenDissociateRequest request = tokenDissociateCaptor.getValue();
        Assertions.assertEquals(List.of(tokenId), request.tokenIds());
        Assertions.assertEquals(accountId, request.accountId());
        Assertions.assertEquals(accountKey, request.accountKey());
    }

    @Test
    void testDissociateTokenThrowsExceptionForInvalidId() throws HieroException {
        final TokenId tokenId = TokenId.fromString("1.2.3");
        final AccountId accountId = AccountId.fromString("1.2.3");
        final PrivateKey accountKey = PrivateKey.generateECDSA();
        final Account account = new Account(accountId, accountKey.getPublicKey(), accountKey);

        when(protocolLayerClient.executeTokenDissociateTransaction(any(TokenDissociateRequest.class)))
                .thenThrow(new HieroException("Failed to execute transaction of type TokenDissociateTransaction"));

        Assertions.assertThrows(HieroException.class, () -> fungibleClientImpl.dissociateToken(tokenId, accountId, accountKey));
        Assertions.assertThrows(HieroException.class, () -> fungibleClientImpl.dissociateToken(tokenId, account));
    }

    @Test
    void testDissociateTokenNullParam() {
        Assertions.assertThrows(NullPointerException.class,
                () -> fungibleClientImpl.dissociateToken((TokenId) null, null, null));
        Assertions.assertThrows(NullPointerException.class,
                () -> fungibleClientImpl.dissociateToken((TokenId) null, null));
    }

    @Test
    void testDissociateTokenWithMultipleToken() throws HieroException {
        final TokenDissociateResult tokenDissociateResult = Mockito.mock(TokenDissociateResult.class);

        final TokenId tokenId1 = TokenId.fromString("1.2.3");
        final TokenId tokenId2 = TokenId.fromString("1.2.4");

        final AccountId accountId = AccountId.fromString("1.2.3");
        final PrivateKey accountKey = PrivateKey.generateECDSA();

        when(protocolLayerClient.executeTokenDissociateTransaction(any(TokenDissociateRequest.class)))
                .thenReturn(tokenDissociateResult);

        fungibleClientImpl.dissociateToken(List.of(tokenId1, tokenId2), accountId, accountKey);

        verify(protocolLayerClient, times(1))
                .executeTokenDissociateTransaction(tokenDissociateCaptor.capture());

        final TokenDissociateRequest request = tokenDissociateCaptor.getValue();
        Assertions.assertEquals(List.of(tokenId1, tokenId2), request.tokenIds());
        Assertions.assertEquals(accountId, request.accountId());
        Assertions.assertEquals(accountKey, request.accountKey());
    }

    @Test
    void testDissociateTokenWithMultipleTokenThrowExceptionIfListEmpty() {
        final AccountId accountId = AccountId.fromString("1.2.3");
        final PrivateKey accountKey = PrivateKey.generateECDSA();

        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class,
                () -> fungibleClientImpl.dissociateToken(List.of(), accountId, accountKey));
        Assertions.assertEquals("tokenIds must not be empty", e.getMessage());
    }
}
